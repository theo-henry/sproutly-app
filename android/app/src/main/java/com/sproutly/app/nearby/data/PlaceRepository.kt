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
import com.sproutly.app.nearby.model.DietFocus
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
    ): List<Place> = osmService.searchAround(origin, filters)
        .filterBy(filters)
        .sortedBy { it.distanceKm }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestDeviceLocation(): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        return client.lastLocation.awaitNullable()
            ?: client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).awaitNullable()
    }
}

class OsmPlaceService {
    private val json = Json { ignoreUnknownKeys = true }
    private var lastCache: SearchCache? = null

    /**
     * Endpoints tried in order. The Kumi mirror tends to respond quickly when the
     * primary endpoint is rate-limiting (429) or timing out (504).
     */
    private val endpoints: List<String> = listOfNotNull(
        AppConfig.overpassEndpoint.takeIf { it.isNotBlank() },
        "https://overpass.kumi.systems/api/interpreter",
        "https://overpass-api.de/api/interpreter",
    ).distinct()

    suspend fun searchAround(origin: GeoPoint, filters: NearbyFilters): List<Place> {
        val radiusMeters = (filters.maxDistanceKm * 1000).toInt().coerceIn(500, 10000)
        val cacheKey = SearchCacheKey(
            lat = round(origin.lat * 1000) / 1000,
            lng = round(origin.lng * 1000) / 1000,
            radiusMeters = radiusMeters,
            diet = filters.dietFocus,
        )
        lastCache?.takeIf { it.key == cacheKey }?.let { return it.places }

        val query = buildQuery(origin, radiusMeters, filters.dietFocus)
        val response = fetchOverpassWithFallback(query)
        val places = response.elements
            .mapNotNull { it.toPlace(origin) }
            .distinctBy { "${it.osmType}:${it.osmId}" }

        lastCache = SearchCache(cacheKey, places)
        return places
    }

    private suspend fun fetchOverpassWithFallback(query: String): OverpassResponse {
        var lastError: IOException? = null
        for (endpoint in endpoints) {
            try {
                return fetchOverpass(endpoint, query)
            } catch (io: IOException) {
                lastError = io
            }
        }
        throw lastError ?: IOException("All Overpass endpoints failed.")
    }

    private suspend fun fetchOverpass(endpoint: String, query: String): OverpassResponse =
        withContext(Dispatchers.IO) {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection)
            val body = "data=" + URLEncoder.encode(query, "UTF-8")

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 10_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("User-Agent", AppConfig.MAP_USER_AGENT)
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")

            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            connection.disconnect()

            if (status !in 200..299) {
                val snippet = text.take(160).replace('\n', ' ').trim()
                throw IOException("Overpass HTTP $status${if (snippet.isNotEmpty()) " · $snippet" else ""}")
            }

            json.decodeFromString(OverpassResponse.serializer(), text)
        }

    /**
     * Lean query — one `nwr` block per concept, no broad regex. Stays well under
     * the 25s server timeout and the typical rate limits on the public endpoint.
     *
     * `dietFocus = VEGAN` adds `diet:vegan=yes/only/limited`. `VEGETARIAN`/`FLEXIBLE`
     * additionally accept `diet:vegetarian=*`. Plant-based supermarket-ish shops
     * (health_food/organic/greengrocer) plus general supermarkets are always
     * included; they're filtered out later by [NearbyFilters.supermarkets].
     */
    private fun buildQuery(origin: GeoPoint, radiusMeters: Int, dietFocus: DietFocus): String {
        val around = "around:$radiusMeters,${origin.lat},${origin.lng}"
        val vegetarianLine = if (dietFocus == DietFocus.VEGAN) {
            ""
        } else {
            """nwr($around)["amenity"~"^(restaurant|cafe|fast_food)$"]["diet:vegetarian"~"^(yes|only|limited)$"];"""
        }
        return """
            [out:json][timeout:20];
            (
              nwr($around)["amenity"~"^(restaurant|cafe|fast_food)$"]["diet:vegan"~"^(yes|only|limited)$"];
              $vegetarianLine
              nwr($around)["shop"~"^(health_food|organic|greengrocer)$"];
              nwr($around)["shop"="supermarket"];
            );
            out center 80;
        """.trimIndent()
    }
}

@Serializable
private data class OverpassResponse(val elements: List<OverpassElement> = emptyList())

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
private data class OverpassCenter(val lat: Double, val lon: Double)

private data class SearchCacheKey(
    val lat: Double,
    val lng: Double,
    val radiusMeters: Int,
    val diet: DietFocus,
)

private data class SearchCache(val key: SearchCacheKey, val places: List<Place>)

private fun OverpassElement.toPlace(origin: GeoPoint): Place? {
    val point = point() ?: return null
    val name = tags["name"] ?: tags["brand"] ?: return null
    val amenity = tags["amenity"]
    val shop = tags["shop"]
    val dietVegetarian = tags["diet:vegetarian"]
    val dietVegan = tags["diet:vegan"]
    val kind = classifyPlace(amenity, shop, dietVegetarian, dietVegan, tags)
    val distance = distanceKm(origin, point)

    return Place(
        id = "$type-$id",
        name = name,
        tagline = buildTagline(kind, tags),
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
    val c = center ?: return null
    return GeoPoint(c.lat, c.lon)
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
    return if (amenity == "restaurant" || amenity == "cafe" || amenity == "fast_food") {
        PlaceKind.RESTAURANT
    } else PlaceKind.PLANT_FRIENDLY
}

private fun buildTagline(kind: PlaceKind, tags: Map<String, String>): String {
    val cuisine = tags["cuisine"]?.replace(";", ", ")
    return when (kind) {
        PlaceKind.FULLY_PLANT_BASED -> "Fully plant-based${cuisine?.let { " · $it" }.orEmpty()}"
        PlaceKind.PLANT_FRIENDLY -> "Vegan/vegetarian options${cuisine?.let { " · $it" }.orEmpty()}"
        PlaceKind.SUPERMARKET -> when (tags["shop"]) {
            "health_food" -> "Health-food shop"
            "organic" -> "Organic shop"
            "greengrocer" -> "Greengrocer"
            else -> "Supermarket"
        }
        PlaceKind.RESTAURANT -> "Restaurant${cuisine?.let { " · $it" }.orEmpty()}"
    }
}

private fun confidenceFor(kind: PlaceKind, tags: Map<String, String>): Double = when {
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
                (filters.plantFriendly && place.kind in setOf(PlaceKind.PLANT_FRIENDLY, PlaceKind.FULLY_PLANT_BASED)) ||
                (filters.supermarkets && place.kind == PlaceKind.SUPERMARKET) ||
                (filters.restaurants && place.kind in setOf(
                    PlaceKind.RESTAURANT, PlaceKind.PLANT_FRIENDLY, PlaceKind.FULLY_PLANT_BASED
                ))) &&
            (!filters.openNow || place.isOpenNow)
    }
}

private fun distanceKm(from: GeoPoint, to: GeoPoint): Double {
    val radiusKm = 6371.0
    val dLat = Math.toRadians(to.lat - from.lat)
    val dLng = Math.toRadians(to.lng - from.lng)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(from.lat)) * cos(Math.toRadians(to.lat)) * sin(dLng / 2).pow(2)
    return radiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private suspend fun <T> Task<T>.awaitNullable(): T? =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { value -> if (continuation.isActive) continuation.resume(value) }
        addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
        addOnCanceledListener { if (continuation.isActive) continuation.resume(null) }
    }
