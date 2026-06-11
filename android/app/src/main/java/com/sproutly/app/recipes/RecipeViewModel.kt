package com.sproutly.app.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sproutly.app.recipes.data.RecipeRepository
import com.sproutly.app.recipes.model.Recipe
import com.sproutly.app.recipes.model.RecipeDietLabel
import com.sproutly.app.recipes.model.RecipeFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class RecipesUiState(
    val featured: Recipe? = null,
    val recipes: List<Recipe> = emptyList(),
    val visibleRecipes: List<Recipe> = emptyList(),
    val quickRecipes: List<Recipe> = emptyList(),
    val filters: List<RecipeFilter> = RecipeFilter.entries,
    val selectedFilter: RecipeFilter = RecipeFilter.ALL,
    val query: String = "",
    val selectedRecipe: Recipe? = null,
    val savedRecipeIds: Set<String> = emptySet(),
)

class RecipeViewModel(
    private val repo: RecipeRepository = RecipeRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(RecipesUiState())
    val state: StateFlow<RecipesUiState> = _state.asStateFlow()

    init { load() }

    fun updateQuery(query: String) {
        _state.value = _state.value.copy(query = query).withFilteredRecipes()
    }

    fun selectFilter(filter: RecipeFilter) {
        _state.value = _state.value.copy(selectedFilter = filter).withFilteredRecipes()
    }

    fun openRecipe(recipe: Recipe) {
        _state.value = _state.value.copy(selectedRecipe = recipe)
    }

    fun closeRecipe() {
        _state.value = _state.value.copy(selectedRecipe = null)
    }

    fun toggleSaved(recipe: Recipe) {
        val saved = _state.value.savedRecipeIds
        _state.value = _state.value.copy(
            savedRecipeIds = if (recipe.id in saved) saved - recipe.id else saved + recipe.id,
        )
    }

    private fun load() {
        viewModelScope.launch {
            val recipes = repo.recipes()
            _state.value = RecipesUiState(
                featured = repo.featured(),
                recipes = recipes,
                visibleRecipes = recipes,
                quickRecipes = recipes.filter { it.totalMinutes <= 20 }.take(4),
            )
        }
    }
}

private fun RecipesUiState.withFilteredRecipes(): RecipesUiState {
    val normalizedQuery = query.trim().lowercase(Locale.US)
    return copy(
        visibleRecipes = recipes.filter { recipe ->
            recipe.matchesFilter(selectedFilter) && (
                normalizedQuery.isBlank() || recipe.searchText().contains(normalizedQuery)
            )
        }
    )
}

private fun Recipe.matchesFilter(filter: RecipeFilter): Boolean =
    when (filter) {
        RecipeFilter.ALL -> true
        RecipeFilter.VEGAN -> dietLabels.contains(RecipeDietLabel.VEGAN)
        RecipeFilter.VEGETARIAN -> dietLabels.contains(RecipeDietLabel.VEGETARIAN)
        RecipeFilter.MOSTLY_PLANT_BASED -> dietLabels.contains(RecipeDietLabel.MOSTLY_PLANT_BASED)
        RecipeFilter.FLEXITARIAN -> dietLabels.contains(RecipeDietLabel.FLEXITARIAN)
        RecipeFilter.WHOLE_FOOD_PLANT_BASED -> dietLabels.contains(RecipeDietLabel.WHOLE_FOOD_PLANT_BASED)
        else -> tags.any { it.equals(filter.label, ignoreCase = true) }
    }

private fun Recipe.searchText(): String =
    buildList {
        add(title)
        add(description)
        add(difficulty)
        add(mealType)
        addAll(tags)
        addAll(dietLabels.map { it.label })
        addAll(ingredients)
    }.joinToString(" ").lowercase(Locale.US)
