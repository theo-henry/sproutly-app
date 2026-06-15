package com.sproutly.app.nearby.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
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
import kotlinx.coroutines.withTimeoutOrNull
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
        if (!hasLocationPermission()) {
            Log.d(TAG, "currentLocation: no permission granted")
            return null
        }
        // Cap the whole lookup so a stuck GPS fix can't block the Madrid fallback.
        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            requestDeviceLocation()?.let { GeoPoint(it.latitude, it.longitude) }
        }.also {
            if (it == null) Log.d(TAG, "currentLocation: no fix within ${LOCATION_TIMEOUT_MS}ms — using Madrid fallback")
            else Log.d(TAG, "currentLocation: got fix ${it.lat},${it.lng}")
        }
    }

    suspend fun nearby(
        origin: GeoPoint,
        filters: NearbyFilters = NearbyFilters(),
    ): NearbySearchResult {
        val requestedRadius = filters.maxDistanceKm.coerceIn(MIN_RADIUS_KM, EXPANDED_RADIUS_KM)
        val requestedFilters = filters.copy(maxDistanceKm = requestedRadius)
        val initial = placesForRadius(origin, requestedFilters)

        if (requestedRadius < EXPANDED_RADIUS_KM && initial.size < MIN_RESULTS_BEFORE_EXPANDING) {
            val expandedFilters = filters.copy(maxDistanceKm = EXPANDED_RADIUS_KM)
            val expanded = placesForRadius(origin, expandedFilters)
            val merged = (expanded + initial)
                .distinctBy { it.id }
                .sortedBy { it.distanceKm }
            return NearbySearchResult(
                places = merged,
                radiusKm = EXPANDED_RADIUS_KM,
                expanded = true,
            )
        }

        return NearbySearchResult(
            places = initial,
            radiusKm = requestedRadius,
            expanded = false,
        )
    }

    private suspend fun placesForRadius(
        origin: GeoPoint,
        filters: NearbyFilters,
    ): List<Place> {
        val overpass = try {
            osmService.searchAround(origin, filters)
        } catch (cancel: kotlinx.coroutines.CancellationException) {
            throw cancel
        } catch (error: Exception) {
            Log.w(TAG, "nearby: Overpass failed — falling back to curated list (${error.message})")
            emptyList()
        }
        // Demo-safe fallback: use the curated Madrid list any time Overpass
        // came back empty *or* errored. The downstream distance filter still
        // applies, so far-away users naturally see "no matches" rather than
        // Madrid pins on a foreign map.
        val pool = if (overpass.isNotEmpty()) overpass else MadridFallback.places(origin)
        if (overpass.isEmpty()) Log.d(TAG, "nearby: using ${pool.size} curated places (origin ${origin.lat},${origin.lng})")
        return pool.filterBy(filters).sortedBy { it.distanceKm }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Walk a chain of location sources from best→broadest. Many real devices
     * (emulators, phones without Play Services, indoor with weak GPS) never
     * return a HIGH_ACCURACY fix; falling through to balanced fused location
     * and finally the platform [LocationManager] keeps the dot off Madrid for
     * the cases where we actually can place the user.
     */
    @SuppressLint("MissingPermission")
    private suspend fun requestDeviceLocation(): Location? {
        val fused = try {
            LocationServices.getFusedLocationProviderClient(context)
        } catch (t: Throwable) {
            Log.d(TAG, "fused client unavailable: ${t.message}")
            null
        }

        // 1. Cached last fused location is usually instant.
        if (fused != null) {
            try {
                fused.lastLocation.awaitNullable()?.takeIf { it.isUsable("fused.lastLocation") }?.let {
                    Log.d(TAG, "location: fused.lastLocation ${it.latitude},${it.longitude}")
                    return it
                }
            } catch (t: Throwable) {
                Log.d(TAG, "fused.lastLocation failed: ${t.message}")
            }
        }

        // 2. Ask for a fresh fused fix at high accuracy.
        if (fused != null) {
            currentFusedLocation(fused, Priority.PRIORITY_HIGH_ACCURACY)
                ?.takeIf { it.isUsable("fused.HIGH") }
                ?.let {
                    Log.d(TAG, "location: fused.getCurrentLocation HIGH_ACCURACY ${it.latitude},${it.longitude}")
                    return it
                }
        }

        // 3. Fall back to balanced power — emulators / weak GPS often only
        //    deliver a network-derived fix.
        if (fused != null) {
            currentFusedLocation(fused, Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                ?.takeIf { it.isUsable("fused.BALANCED") }
                ?.let {
                    Log.d(TAG, "location: fused.getCurrentLocation BALANCED ${it.latitude},${it.longitude}")
                    return it
                }
        }

        // 4. As a last resort, use the platform LocationManager directly —
        //    works on devices without Google Play Services.
        platformLastKnown()?.takeIf { it.isUsable("platform") }?.let {
            Log.d(TAG, "location: platform LocationManager ${it.latitude},${it.longitude}")
            return it
        }

        return null
    }

    /**
     * Reject obviously-bogus fixes that emulators / passive providers love to
     * hand back: (0,0) sentinels, "accuracy worse than a small city" results,
     * and Locations more than a day old. Without this filter the app honors
     * a fake (0,0) Location, the map flies mid-ocean, and every curated
     * Madrid place gets stripped by the distance filter.
     */
    private fun Location.isUsable(label: String): Boolean {
        if (latitude == 0.0 && longitude == 0.0) {
            Log.d(TAG, "$label: rejected (0,0) sentinel")
            return false
        }
        if (latitude.isNaN() || longitude.isNaN()) {
            Log.d(TAG, "$label: rejected NaN coords")
            return false
        }
        if (hasAccuracy() && accuracy > 5_000f) {
            Log.d(TAG, "$label: rejected, accuracy=${accuracy}m")
            return false
        }
        val ageMs = System.currentTimeMillis() - time
        if (ageMs > 24L * 60 * 60 * 1000) {
            Log.d(TAG, "$label: rejected, age=${ageMs / 60_000}min")
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentFusedLocation(
        client: com.google.android.gms.location.FusedLocationProviderClient,
        priority: Int,
    ): Location? {
        val tokenSource = CancellationTokenSource()
        return try {
            client.getCurrentLocation(priority, tokenSource.token).awaitNullable()
        } catch (t: Throwable) {
            Log.d(TAG, "fused getCurrentLocation($priority) failed: ${t.message}")
            null
        } finally {
            tokenSource.cancel()
        }
    }

    @SuppressLint("MissingPermission")
    private fun platformLastKnown(): Location? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }
        return providers
            .mapNotNull { p ->
                runCatching { manager.getLastKnownLocation(p) }
                    .onFailure { Log.d(TAG, "platform provider $p failed: ${it.message}") }
                    .getOrNull()
            }
            .maxByOrNull { it.time }
    }

    private companion object {
        const val LOCATION_TIMEOUT_MS = 8_000L
        const val MIN_RESULTS_BEFORE_EXPANDING = 5
        const val MIN_RADIUS_KM = 0.5
        const val EXPANDED_RADIUS_KM = 10.0
        const val TAG = "PlaceRepository"
    }
}

data class NearbySearchResult(
    val places: List<Place>,
    val radiusKm: Double,
    val expanded: Boolean,
)

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

        // Only cache non-empty Overpass responses so a one-off empty result
        // (transient hiccup) doesn't lock the user out of the demo fallback.
        if (places.isNotEmpty()) lastCache = SearchCache(cacheKey, places)
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

/**
 * Curated set of well-known Madrid plant-based and plant-friendly spots used as
 * a demo-safe fallback when the Overpass API is unreachable (rate-limited, slow,
 * or blocked by the local network). Coordinates are approximate — they're only
 * used to pin the markers on the map and to compute a distance label.
 */
private object MadridFallback {
    private data class Seed(
        val name: String,
        val tagline: String,
        val kind: PlaceKind,
        val lat: Double,
        val lng: Double,
        val address: String? = null,
    )

    private val seeds: List<Seed> = listOf(
        Seed("Distrito Vegano", "Fully plant-based · global", PlaceKind.FULLY_PLANT_BASED,
            40.4093, -3.7019, "Calle del Doctor Fourquet, Lavapiés"),
        Seed("El Vergel", "Fully plant-based · seasonal", PlaceKind.FULLY_PLANT_BASED,
            40.4309, -3.7039, "Chamberí"),
        Seed("Loving Hut Madrid", "Fully plant-based · Asian", PlaceKind.FULLY_PLANT_BASED,
            40.4282, -3.7034, "Calle de Hortaleza"),
        Seed("Vega", "Fully plant-based · brunch & dinner", PlaceKind.FULLY_PLANT_BASED,
            40.4267, -3.6948, "Calle de la Luna, Centro"),
        Seed("Mamá Campo", "Vegetarian-friendly · market kitchen", PlaceKind.PLANT_FRIENDLY,
            40.4373, -3.7028, "Plaza de Olavide, Chamberí"),
        Seed("Veggie Garden", "Fully plant-based · burgers & bowls", PlaceKind.FULLY_PLANT_BASED,
            40.4253, -3.7081, "Malasaña"),
        Seed("B13 Bar", "Plant-based bar · tapas", PlaceKind.FULLY_PLANT_BASED,
            40.4079, -3.7048, "Calle de la Cabeza, Lavapiés"),
        Seed("Veganitessen", "Plant-based bakery", PlaceKind.SUPERMARKET,
            40.4154, -3.7099, "La Latina"),
        Seed("NaturaSí Madrid", "Organic supermarket", PlaceKind.SUPERMARKET,
            40.4341, -3.6906, "Salesas"),
        Seed("Casa Ruiz Bio", "Organic & bulk shop", PlaceKind.SUPERMARKET,
            40.4316, -3.7044, "Chamberí"),
        Seed("Sala de Despiece — Veggie", "Veg options · contemporary", PlaceKind.PLANT_FRIENDLY,
            40.4374, -3.7000, "Calle de Ponzano"),
        Seed("La Encomienda de Almodóvar", "Vegetarian-friendly · Spanish", PlaceKind.PLANT_FRIENDLY,
            40.4172, -3.7110, "La Latina"),
    )

    fun places(origin: GeoPoint): List<Place> = seeds.mapIndexed { idx, seed ->
        val point = GeoPoint(seed.lat, seed.lng)
        val distance = distanceKm(origin, point)
        Place(
            id = "madrid-fallback-$idx",
            name = seed.name,
            tagline = seed.tagline,
            distanceKm = round(distance * 10) / 10,
            kind = seed.kind,
            isOpenNow = true,
            lat = seed.lat,
            lng = seed.lng,
            osmType = null,
            osmId = null,
            address = seed.address,
            dietVegetarian = if (seed.kind == PlaceKind.FULLY_PLANT_BASED) "only" else "yes",
            dietVegan = when (seed.kind) {
                PlaceKind.FULLY_PLANT_BASED -> "only"
                PlaceKind.PLANT_FRIENDLY -> "yes"
                else -> null
            },
            amenity = if (seed.kind == PlaceKind.SUPERMARKET) null else "restaurant",
            shop = if (seed.kind == PlaceKind.SUPERMARKET) "organic" else null,
            source = "Sproutly curated · Madrid",
            confidence = 0.9,
        )
    }
}

private suspend fun <T> Task<T>.awaitNullable(): T? =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { value -> if (continuation.isActive) continuation.resume(value) }
        addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
        addOnCanceledListener { if (continuation.isActive) continuation.resume(null) }
    }
