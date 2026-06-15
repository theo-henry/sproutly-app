package com.sproutly.app.nearby

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sproutly.app.core.config.AppConfig
import com.sproutly.app.core.result.AppResult
import com.sproutly.app.nearby.data.PlaceRepository
import com.sproutly.app.nearby.model.DietFocus
import com.sproutly.app.nearby.model.GeoPoint
import com.sproutly.app.nearby.model.LocationSource
import com.sproutly.app.nearby.model.NearbyFilters
import com.sproutly.app.nearby.model.Place
import com.sproutly.app.profile.data.ProfileRepository
import com.sproutly.app.profile.model.DietPreference
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NearbyUiState(
    val filters: NearbyFilters = NearbyFilters(maxDistanceKm = 5.0),
    val effectiveRadiusKm: Double = filters.maxDistanceKm,
    val places: List<Place> = emptyList(),
    val origin: GeoPoint = GeoPoint(AppConfig.MADRID_LAT, AppConfig.MADRID_LNG),
    val locationSource: LocationSource = LocationSource.MADRID_FALLBACK,
    val loading: Boolean = false,
    val fallbackNoticeId: Int = 0,
    val requestingLocationPermission: Boolean = false,
    val error: String? = null,
    val dietPreferenceLabel: String? = null,
)

class NearbyViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PlaceRepository(application.applicationContext)
    private val profileRepo = ProfileRepository()

    private val _state = MutableStateFlow(NearbyUiState())
    val state: StateFlow<NearbyUiState> = _state.asStateFlow()

    private var initialized = false
    private var reloadJob: Job? = null

    fun loadInitial(hasLocationPermission: Boolean) {
        if (initialized) return
        initialized = true

        if (!hasLocationPermission) {
            _state.value = _state.value.copy(requestingLocationPermission = true)
            reload(useDeviceLocation = false, refreshDietPreference = true)
            return
        }
        reload(useDeviceLocation = true, refreshDietPreference = true)
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _state.value = _state.value.copy(requestingLocationPermission = false)
        reload(useDeviceLocation = granted, refreshDietPreference = true)
    }

    fun refresh() {
        reload(
            useDeviceLocation = _state.value.locationSource == LocationSource.DEVICE,
            refreshDietPreference = true,
        )
    }

    fun setFilters(filters: NearbyFilters) {
        _state.value = _state.value.copy(filters = filters)
        reload(useDeviceLocation = _state.value.locationSource == LocationSource.DEVICE)
    }

    fun focusSupermarkets() {
        val current = _state.value
        _state.value = current.copy(
            filters = current.filters.copy(
                fullyPlantBased = false,
                plantFriendly = false,
                supermarkets = true,
                restaurants = false,
                openNow = false,
            )
        )
        if (initialized) {
            reload(useDeviceLocation = _state.value.locationSource == LocationSource.DEVICE)
        }
    }

    private suspend fun applyDietPreference() {
        when (val r = profileRepo.getCurrent()) {
            is AppResult.Success -> {
                val pref = DietPreference.fromValue(r.data?.dietPreference)
                val focus = pref.toDietFocus()
                _state.value = _state.value.copy(
                    filters = _state.value.filters.copy(dietFocus = focus),
                    dietPreferenceLabel = pref?.label,
                )
            }
            is AppResult.Failure -> Unit // keep existing focus; not fatal
        }
    }

    private fun reload(
        useDeviceLocation: Boolean,
        refreshDietPreference: Boolean = false,
    ) {
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            if (refreshDietPreference) applyDietPreference()

            val requestedFilters = _state.value.filters
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                effectiveRadiusKm = requestedFilters.maxDistanceKm,
            )

            val deviceLocation = if (useDeviceLocation) repo.currentLocation() else null
            val origin = deviceLocation ?: GeoPoint(AppConfig.MADRID_LAT, AppConfig.MADRID_LNG)
            val source = if (deviceLocation != null) LocationSource.DEVICE else LocationSource.MADRID_FALLBACK
            val fallbackNoticeId = if (useDeviceLocation && deviceLocation == null) {
                _state.value.fallbackNoticeId + 1
            } else {
                _state.value.fallbackNoticeId
            }

            try {
                val result = repo.nearby(origin, requestedFilters)
                _state.value = _state.value.copy(
                    places = result.places,
                    origin = origin,
                    locationSource = source,
                    loading = false,
                    effectiveRadiusKm = result.radiusKm,
                    fallbackNoticeId = fallbackNoticeId,
                    error = null,
                )
            } catch (cancel: kotlinx.coroutines.CancellationException) {
                throw cancel
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    places = emptyList(),
                    origin = origin,
                    locationSource = source,
                    loading = false,
                    effectiveRadiusKm = requestedFilters.maxDistanceKm,
                    fallbackNoticeId = fallbackNoticeId,
                    error = error.message ?: "Could not load nearby places.",
                )
            }
        }
    }
}

private fun DietPreference?.toDietFocus(): DietFocus = when (this) {
    DietPreference.VEGAN, DietPreference.WHOLE_FOOD_PLANT_BASED -> DietFocus.VEGAN
    DietPreference.VEGETARIAN, DietPreference.MOSTLY_PLANT_BASED -> DietFocus.VEGETARIAN
    DietPreference.FLEXITARIAN, DietPreference.OTHER, null -> DietFocus.FLEXIBLE
}
