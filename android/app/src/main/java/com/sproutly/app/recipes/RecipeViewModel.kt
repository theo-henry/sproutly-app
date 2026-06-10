package com.sproutly.app.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sproutly.app.recipes.data.RecipeRepository
import com.sproutly.app.recipes.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipesUiState(
    val featured: Recipe? = null,
    val quick: List<Recipe> = emptyList(),
    val seasonal: List<Recipe> = emptyList(),
)

class RecipeViewModel(
    private val repo: RecipeRepository = RecipeRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(RecipesUiState())
    val state: StateFlow<RecipesUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.value = RecipesUiState(
                featured = repo.featured(),
                quick = repo.quick(),
                seasonal = repo.seasonal(),
            )
        }
    }
}
