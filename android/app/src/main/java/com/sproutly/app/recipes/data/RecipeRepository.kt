package com.sproutly.app.recipes.data

import com.sproutly.app.recipes.model.Recipe

/**
 * Placeholder repository. Future: search, saved, generated, pantry-aware, diet-filtered.
 */
class RecipeRepository {

    suspend fun featured(): Recipe = Recipe(
        id = "tonight-1",
        title = "Smoky tempeh tacos",
        subtitle = "High-protein, low-prep, no compromise.",
        prepMinutes = 25,
        proteinGrams = 30,
        tags = listOf("High-protein", "Quick"),
    )

    suspend fun quick(): List<Recipe> = listOf(
        Recipe("q1", "15-min peanut noodles", prepMinutes = 15),
        Recipe("q2", "Lemon herb tofu wrap", prepMinutes = 18),
        Recipe("q3", "Chickpea shakshuka", prepMinutes = 20),
    )

    suspend fun seasonal(): List<Recipe> = listOf(
        Recipe("s1", "Roasted squash"),
        Recipe("s2", "Charred greens salad"),
    )

    // TODO: searchByQuery(), savedRecipes(), generateFromPantry(ingredients), filterByDiet(...)
}
