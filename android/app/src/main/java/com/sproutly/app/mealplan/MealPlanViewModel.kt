package com.sproutly.app.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sproutly.app.core.result.AppResult
import com.sproutly.app.core.result.UiState
import com.sproutly.app.mealplan.data.MealPlanRepository
import com.sproutly.app.mealplan.model.MealPlan
import com.sproutly.app.mealplan.model.MealPlanDay
import com.sproutly.app.mealplan.model.MealPlanFactory
import com.sproutly.app.mealplan.model.MealSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealPlanViewModel(
    private val repo: MealPlanRepository = MealPlanRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<MealPlan>>(UiState.Loading)
    val state: StateFlow<UiState<MealPlan>> = _state.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val weekStart = MealPlanFactory.currentMonday().toString()
            when (val r = repo.getForWeek(weekStart)) {
                is AppResult.Success -> _state.value = UiState.Success(r.data ?: MealPlanFactory.emptyPlan())
                is AppResult.Failure -> _state.value = UiState.Success(MealPlanFactory.emptyPlan())
            }
        }
    }

    fun generateStarter() {
        _state.value = UiState.Success(MealPlanFactory.starterPlan())
    }

    fun updateMeal(dayIndex: Int, slot: MealSlot, text: String) {
        val current = (state.value as? UiState.Success)?.data ?: return
        val days = current.days.toMutableList()
        val day = days[dayIndex]
        val meals = day.meals.toMutableMap()
        meals[slot.key] = text
        days[dayIndex] = MealPlanDay(date = day.date, meals = meals)
        _state.value = UiState.Success(current.copy(days = days))
    }

    fun save() {
        val current = (state.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            when (val r = repo.upsert(current)) {
                is AppResult.Success -> _message.value = "Meal plan saved"
                is AppResult.Failure -> _message.value = "Save failed: ${r.message}"
            }
        }
    }

    fun clearMessage() { _message.value = null }
}
