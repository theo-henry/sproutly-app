package com.sproutly.app.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sproutly.app.auth.data.DemoAccountStore
import com.sproutly.app.core.network.SupabaseClientProvider
import com.sproutly.app.core.result.AppResult
import com.sproutly.app.core.result.UiState
import com.sproutly.app.mealplan.data.MealPlanRepository
import com.sproutly.app.mealplan.model.MealPlan
import com.sproutly.app.mealplan.model.MealPlanFactory
import com.sproutly.app.profile.data.ProfileRepository
import com.sproutly.app.profile.model.Profile
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repo: ProfileRepository = ProfileRepository(),
    private val mealPlanRepo: MealPlanRepository = MealPlanRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Profile>>(UiState.Loading)
    val state: StateFlow<UiState<Profile>> = _state.asStateFlow()

    private val _savedMessage = MutableStateFlow<String?>(null)
    val savedMessage: StateFlow<String?> = _savedMessage.asStateFlow()

    private val _mealPlan = MutableStateFlow<UiState<MealPlan?>>(UiState.Loading)
    val mealPlan: StateFlow<UiState<MealPlan?>> = _mealPlan.asStateFlow()

    init {
        load()
        loadMealPlan()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val r = repo.getCurrent()) {
                is AppResult.Success -> {
                    val profile = r.data ?: defaultProfile()
                    _state.value = UiState.Success(profile)
                }
                is AppResult.Failure -> _state.value = UiState.Error(r.message)
            }
        }
    }

    fun loadMealPlan() {
        viewModelScope.launch {
            _mealPlan.value = UiState.Loading
            val weekStart = MealPlanFactory.currentMonday().toString()
            when (val r = mealPlanRepo.getForWeek(weekStart)) {
                is AppResult.Success -> _mealPlan.value = UiState.Success(r.data)
                is AppResult.Failure -> _mealPlan.value = UiState.Error(r.message)
            }
        }
    }

    private fun defaultProfile(): Profile {
        if (DemoAccountStore.isEnabled()) return DemoAccountStore.getProfile()
        val user = SupabaseClientProvider.client.auth.currentUserOrNull()
        return Profile(
            id = user?.id ?: "",
            email = user?.email,
        )
    }

    fun update(transform: (Profile) -> Profile) {
        val current = (state.value as? UiState.Success)?.data ?: return
        _state.value = UiState.Success(transform(current))
    }

    fun save() {
        val current = (state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            when (val r = repo.upsert(current)) {
                is AppResult.Success -> {
                    _state.value = UiState.Success(r.data)
                    _savedMessage.value = "Profile saved"
                }
                is AppResult.Failure -> _savedMessage.value = "Save failed: ${r.message}"
            }
        }
    }

    fun clearSavedMessage() { _savedMessage.value = null }

    fun avatarUrl(path: String?): String? = path?.let { repo.avatarPublicUrl(it) }

    fun uploadAvatar(bytes: ByteArray) {
        val current = (state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            when (val r = repo.uploadAvatar(current.id, bytes)) {
                is AppResult.Success -> {
                    val updated = current.copy(avatarPath = r.data)
                    _state.value = UiState.Success(updated)
                    save()
                }
                is AppResult.Failure -> _savedMessage.value = "Upload failed: ${r.message}"
            }
        }
    }
}
