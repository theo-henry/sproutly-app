package com.sproutly.app.nearby.model

enum class PlaceKind { FULLY_PLANT_BASED, PLANT_FRIENDLY, SUPERMARKET, RESTAURANT }

data class Place(
    val id: String,
    val name: String,
    val tagline: String,
    val distanceKm: Double,
    val kind: PlaceKind,
    val isOpenNow: Boolean = true,
    val lat: Double? = null,
    val lng: Double? = null,
)

data class NearbyFilters(
    val fullyPlantBased: Boolean = false,
    val plantFriendly: Boolean = false,
    val supermarkets: Boolean = false,
    val restaurants: Boolean = false,
    val openNow: Boolean = false,
    val maxDistanceKm: Double = 5.0,
)
