package com.sproutly.app.nearby.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.sproutly.app.core.config.AppConfig
import com.sproutly.app.nearby.model.GeoPoint
import com.sproutly.app.nearby.model.NearbyFilters
import com.sproutly.app.nearby.model.Place
import com.sproutly.app.nearby.model.PlaceKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

class PlaceRepository(
    private val context: Context,
    private val osmService: OsmPlaceService = OsmPlaceService(),
) {
    suspend fun currentLocation(): GeoPoint? {
        if (!hasLocationPermission()) return null

        return requestDeviceLocation()?.let { GeoPoint(it.latitude, it.longitude) }
    }

    suspend fun nearby(
        origin: GeoPoint,
        filters: NearbyFilters = NearbyFilters(),
    ): List<Place> {
        return osmService.searchAround(origin, filters)
            .filterBy(filters)
            .sortedBy { it.distanceKm }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestDeviceLocation(): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        return client.lastLocation.awaitNullable()
            ?: client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .awaitNullable()
    }
}

class OsmPlaceService {
    private val json = Json { ignoreUnknownKeys = true }
    private var lastCache: SearchCache? = null

    suspend fun searchAround(origin: GeoPoint, filters: NearbyFilters): List<Place> {
        val radiusMeters = (filters.maxDistanceKm * 1000).toInt().coerceIn(500, 10000)
        val cacheKey = SearchCacheKey(
            lat = round(origin.lat * 1000) / 1000,
            lng = round(origin.lng * 1000) / 1000,
            radiusMeters = radiusMeters,
        )

        lastCache?.takeIf { it.key == cacheKey }?.let { return it.places }

        val response = fetchOverpass(buildMadridFirstQuery(origin, radiusMeters))
        val places = response.elements
            .mapNotNull { it.toPlace(origin) }
            .distinctBy { "${it.osmType}:${it.osmId}" }

        lastCache = SearchCache(cacheKey, places)
        return places
    }

    private suspend fun fetchOverpass(query: String): OverpassResponse =
        withContext(Dispatchers.IO) {
            val connection = (URL(AppConfig.overpassEndpoint).openConnection() as HttpURLConnection)
            val body = "data=" + URLEncoder.encode(query, "UTF-8")

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 12_000
            connection.readTimeout = 25_000
            connection.setRequestProperty("User-Agent", AppConfig.MAP_USER_AGENT)
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            connection.outputStream.use { stream ->
                stream.write(body.toByteArray(Charsets.UTF_8))
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream.bufferedReader().use { it.readText() }
            connection.disconnect()

            if (status !in 200..299) {
                throw IOException("OpenStreetMap search failed with HTTP $status.")
            }

            json.decodeFromString(OverpassResponse.serializer(), text)
        }

    private fun buildMadridFirstQuery(origin: GeoPoint, radiusMeters: Int): String {
        val lat = origin.lat
        val lng = origin.lng
        val radius = radiusMeters

        return """
            [out:json][timeout:25];
            (
              node(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["diet:vegetarian"~"^(yes|only|limited)$"];
              way(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["diet:vegetarian"~"^(yes|only|limited)$"];
              relation(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["diet:vegetarian"~"^(yes|only|limited)$"];
              node(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["diet:vegan"~"^(yes|only|limited)$"];
              way(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["diet:vegan"~"^(yes|only|limited)$"];
              relation(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["diet:vegan"~"^(yes|only|limited)$"];
              node(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["cuisine"~"[Vv]egan|[Vv]egetarian|[Vv]egano|[Vv]egetariano"];
              way(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["cuisine"~"[Vv]egan|[Vv]egetarian|[Vv]egano|[Vv]egetariano"];
              relation(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["cuisine"~"[Vv]egan|[Vv]egetarian|[Vv]egano|[Vv]egetariano"];
              node(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["name"~"[Vv]egan|[Vv]egetarian|[Vv]egano|[Vv]egetariano"];
              way(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["name"~"[Vv]egan|[Vv]egetarian|[Vv]egano|[Vv]egetariano"];
              relation(around:$radius,$lat,$lng)["amenity"~"^(restaurant|cafe)$"]["name"~"[Vv]egan|[Vv]egetarian|[Vv]egano|[Vv]egetariano"];
              node(around:$radius,$lat,$lng)["shop"~"^(supermarket|health_food|organic|greengrocer)$"];
              way(around:$radius,$lat,$lng)["shop"~"^(supermarket|health_food|organic|greengrocer)$"];
              relation(around:$radius,$lat,$lng)["shop"~"^(supermarket|health_food|organic|greengrocer)$"];
            );
            out center 100;
        """.trimIndent()
    }
}

@Serializable
private data class OverpassResponse(
    val elements: List<OverpassElement> = emptyList(),
)

@Serializable
private data class OverpassElement(
    val type: String,
    val id: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenter? = null,
    val tags: Map<String, String> = emptyMap(),
)

@Serializable
private data class OverpassCenter(
    val lat: Double,
    val lon: Double,
)

private data class SearchCacheKey(
    val lat: Double,
    val lng: Double,
    val radiusMeters: Int,
)

private data class SearchCache(
    val key: SearchCacheKey,
    val places: List<Place>,
)

private fun OverpassElement.toPlace(origin: GeoPoint): Place? {
    val point = point() ?: return null
    val name = tags["name"] ?: tags["brand"] ?: return null
    val amenity = tags["amenity"]
    val shop = tags["shop"]
    val dietVegetarian = tags["diet:vegetarian"]
    val dietVegan = tags["diet:vegan"]
    val kind = classifyPlace(amenity, shop, dietVegetarian, dietVegan, tags)
    val distance = distanceKm(origin, point)
    val tagline = buildTagline(kind, tags)

    return Place(
        id = "$type-$id",
        name = name,
        tagline = tagline,
        distanceKm = round(distance * 10) / 10,
        kind = kind,
        isOpenNow = true,
        lat = point.lat,
        lng = point.lng,
        osmType = type,
        osmId = id,
        address = addressFrom(tags),
        dietVegetarian = dietVegetarian,
        dietVegan = dietVegan,
        amenity = amenity,
        shop = shop,
        confidence = confidenceFor(kind, tags),
    )
}

private fun OverpassElement.point(): GeoPoint? {
    if (lat != null && lon != null) return GeoPoint(lat, lon)
    val center = center ?: return null
    return GeoPoint(center.lat, center.lon)
}

private fun classifyPlace(
    amenity: String?,
    shop: String?,
    dietVegetarian: String?,
    dietVegan: String?,
    tags: Map<String, String>,
): PlaceKind {
    if (shop != null) return PlaceKind.SUPERMARKET
    if (dietVegan == "only" || dietVegetarian == "only") return PlaceKind.FULLY_PLANT_BASED
    if (dietVegan in setOf("yes", "limited") || dietVegetarian in setOf("yes", "limited")) {
        return PlaceKind.PLANT_FRIENDLY
    }
    if (tags["cuisine"]?.contains(Regex("vegan|vegetarian|vegano|vegetariano", RegexOption.IGNORE_CASE)) == true) {
        return PlaceKind.PLANT_FRIENDLY
    }
    return if (amenity == "restaurant" || amenity == "cafe") PlaceKind.RESTAURANT else PlaceKind.PLANT_FRIENDLY
}

private fun buildTagline(kind: PlaceKind, tags: Map<String, String>): String {
    val cuisine = tags["cuisine"]?.replace(";", ", ")
    val opening = tags["opening_hours"]?.let { " · Hours listed" }.orEmpty()

    return when (kind) {
        PlaceKind.FULLY_PLANT_BASED -> "Fully plant-based${cuisine?.let { " · $it" }.orEmpty()}$opening"
        PlaceKind.PLANT_FRIENDLY -> "Vegetarian/vegan options${cuisine?.let { " · $it" }.orEmpty()}$opening"
        PlaceKind.SUPERMARKET -> "Supermarket${tags["organic"]?.let { " · organic: $it" }.orEmpty()}$opening"
        PlaceKind.RESTAURANT -> "Restaurant${cuisine?.let { " · $it" }.orEmpty()}$opening"
    }
}

private fun confidenceFor(kind: PlaceKind, tags: Map<String, String>): Double =
    when {
        kind == PlaceKind.FULLY_PLANT_BASED -> 0.95
        tags.containsKey("diet:vegan") || tags.containsKey("diet:vegetarian") -> 0.88
        tags.containsKey("cuisine") -> 0.76
        kind == PlaceKind.SUPERMARKET -> 0.7
        else -> 0.62
    }

private fun addressFrom(tags: Map<String, String>): String? {
    val street = tags["addr:street"]
    val number = tags["addr:housenumber"]
    val city = tags["addr:city"]
    return listOfNotNull(
        listOfNotNull(street, number).takeIf { it.isNotEmpty() }?.joinToString(" "),
        city,
    ).takeIf { it.isNotEmpty() }?.joinToString(", ")
}

private fun List<Place>.filterBy(filters: NearbyFilters): List<Place> {
    val hasKindFilter = filters.fullyPlantBased ||
        filters.plantFriendly ||
        filters.supermarkets ||
        filters.restaurants

    return filter { place ->
        place.distanceKm <= filters.maxDistanceKm &&
            (!hasKindFilter ||
                (filters.fullyPlantBased && place.kind == PlaceKind.FULLY_PLANT_BASED) ||
                (filters.plantFriendly && place.kind in setOf(
                    PlaceKind.PLANT_FRIENDLY,
                    PlaceKind.FULLY_PLANT_BASED,
                )) ||
                (filters.supermarkets && place.kind == PlaceKind.SUPERMARKET) ||
                (filters.restaurants && place.kind in setOf(
                    PlaceKind.RESTAURANT,
                    PlaceKind.PLANT_FRIENDLY,
                    PlaceKind.FULLY_PLANT_BASED,
                ))) &&
            (!filters.openNow || place.isOpenNow)
    }
}

private fun distanceKm(from: GeoPoint, to: GeoPoint): Double {
    val radiusKm = 6371.0
    val dLat = Math.toRadians(to.lat - from.lat)
    val dLng = Math.toRadians(to.lng - from.lng)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(from.lat)) *
        cos(Math.toRadians(to.lat)) *
        sin(dLng / 2).pow(2)
    return radiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private suspend fun <T> Task<T>.awaitNullable(): T? =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { value ->
            if (continuation.isActive) continuation.resume(value)
        }
        addOnFailureListener {
            if (continuation.isActive) continuation.resume(null)
        }
        addOnCanceledListener {
            if (continuation.isActive) continuation.resume(null)
        }
    }
