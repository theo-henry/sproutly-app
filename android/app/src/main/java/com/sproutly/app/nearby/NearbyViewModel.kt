package com.sproutly.app.nearby

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sproutly.app.core.config.AppConfig
import com.sproutly.app.nearby.data.PlaceRepository
import com.sproutly.app.nearby.model.GeoPoint
import com.sproutly.app.nearby.model.LocationSource
import com.sproutly.app.nearby.model.NearbyFilters
import com.sproutly.app.nearby.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NearbyUiState(
    val filters: NearbyFilters = NearbyFilters(maxDistanceKm = 5.0),
    val places: List<Place> = emptyList(),
    val origin: GeoPoint = GeoPoint(AppConfig.MADRID_LAT, AppConfig.MADRID_LNG),
    val locationSource: LocationSource = LocationSource.MADRID_FALLBACK,
    val loading: Boolean = false,
    val requestingLocationPermission: Boolean = false,
    val error: String? = null,
)

class NearbyViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PlaceRepository(application.applicationContext)

    private val _state = MutableStateFlow(NearbyUiState())
    val state: StateFlow<NearbyUiState> = _state.asStateFlow()

    private var initialized = false

    fun loadInitial(hasLocationPermission: Boolean) {
        if (initialized) return
        initialized = true

        if (!hasLocationPermission) {
            _state.value = _state.value.copy(requestingLocationPermission = true)
            reload(useDeviceLocation = false)
            return
        }

        reload(useDeviceLocation = true)
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _state.value = _state.value.copy(requestingLocationPermission = false)
        reload(useDeviceLocation = granted)
    }

    fun refresh() {
        reload(useDeviceLocation = _state.value.locationSource == LocationSource.DEVICE)
    }

    fun setFilters(filters: NearbyFilters) {
        _state.value = _state.value.copy(filters = filters)
        reload(useDeviceLocation = _state.value.locationSource == LocationSource.DEVICE)
    }

    private fun reload(useDeviceLocation: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            val deviceLocation = if (useDeviceLocation) repo.currentLocation() else null
            val origin = deviceLocation ?: GeoPoint(AppConfig.MADRID_LAT, AppConfig.MADRID_LNG)
            val source = if (deviceLocation != null) {
                LocationSource.DEVICE
            } else {
                LocationSource.MADRID_FALLBACK
            }

            try {
                val places = repo.nearby(origin, _state.value.filters)
                _state.value = _state.value.copy(
                    places = places,
                    origin = origin,
                    locationSource = source,
                    loading = false,
                    error = null,
                )
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    places = emptyList(),
                    origin = origin,
                    locationSource = source,
                    loading = false,
                    error = error.message ?: "Could not load nearby places.",
                )
            }
        }
    }
}
