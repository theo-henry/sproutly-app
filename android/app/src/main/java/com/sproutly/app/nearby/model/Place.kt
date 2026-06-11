package com.sproutly.app.nearby.model

enum class PlaceKind { FULLY_PLANT_BASED, PLANT_FRIENDLY, SUPERMARKET, RESTAURANT }

data class GeoPoint(
    val lat: Double,
    val lng: Double,
)

enum class LocationSource { DEVICE, MADRID_FALLBACK }

data class Place(
    val id: String,
    val name: String,
    val tagline: String,
    val distanceKm: Double,
    val kind: PlaceKind,
    val isOpenNow: Boolean = true,
    val lat: Double? = null,
    val lng: Double? = null,
    val osmType: String? = null,
    val osmId: Long? = null,
    val address: String? = null,
    val dietVegetarian: String? = null,
    val dietVegan: String? = null,
    val amenity: String? = null,
    val shop: String? = null,
    val source: String = "OpenStreetMap",
    val confidence: Double = 0.7,
)

data class NearbyFilters(
    val fullyPlantBased: Boolean = false,
    val plantFriendly: Boolean = false,
    val supermarkets: Boolean = false,
    val restaurants: Boolean = false,
    val openNow: Boolean = false,
    val maxDistanceKm: Double = 5.0,
)
