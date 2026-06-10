package com.sproutly.app.nearby.data

import com.sproutly.app.nearby.model.NearbyFilters
import com.sproutly.app.nearby.model.Place
import com.sproutly.app.nearby.model.PlaceKind

/**
 * Placeholder repository for nearby plant-based places.
 *
 * Future:
 *  - request runtime ACCESS_FINE_LOCATION
 *  - read current location via FusedLocationProviderClient
 *  - Google Maps Compose pins
 *  - filter by [NearbyFilters]
 *  - real backend: Supabase RPC or Places API
 */
class PlaceRepository {
    suspend fun nearby(filters: NearbyFilters = NearbyFilters()): List<Place> = listOf(
        Place("p1", "Green Garden Bistro", "100% plant-based", 0.4, PlaceKind.FULLY_PLANT_BASED),
        Place("p2", "BioMarket Centre", "Supermarket", 0.8, PlaceKind.SUPERMARKET),
        Place("p3", "Sprout & Co.", "Vegan options", 1.2, PlaceKind.PLANT_FRIENDLY),
    )

    // TODO: suspend fun currentLocation(): LatLng?
    // TODO: suspend fun searchAround(lat: Double, lng: Double, filters: NearbyFilters): List<Place>
}
