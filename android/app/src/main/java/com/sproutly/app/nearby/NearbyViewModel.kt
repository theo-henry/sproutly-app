package com.sproutly.app.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sproutly.app.nearby.data.PlaceRepository
import com.sproutly.app.nearby.model.NearbyFilters
import com.sproutly.app.nearby.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NearbyUiState(
    val filters: NearbyFilters = NearbyFilters(),
    val places: List<Place> = emptyList(),
)

class NearbyViewModel(
    private val repo: PlaceRepository = PlaceRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(NearbyUiState())
    val state: StateFlow<NearbyUiState> = _state.asStateFlow()

    init { reload() }

    fun reload() {
        viewModelScope.launch {
            val places = repo.nearby(_state.value.filters)
            _state.value = _state.value.copy(places = places)
        }
    }
}
