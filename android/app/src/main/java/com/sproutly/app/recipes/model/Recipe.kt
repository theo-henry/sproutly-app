package com.sproutly.app.recipes.model

import kotlinx.serialization.Serializable

@Serializable
enum class RecipeDietLabel(val label: String) {
    VEGAN("Vegan"),
    VEGETARIAN("Vegetarian"),
    MOSTLY_PLANT_BASED("Mostly plant-based"),
    FLEXITARIAN("Flexitarian"),
    WHOLE_FOOD_PLANT_BASED("Whole-food plant-based");
}

enum class RecipeFilter(val label: String) {
    ALL("All"),
    VEGAN("Vegan"),
    VEGETARIAN("Vegetarian"),
    MOSTLY_PLANT_BASED("Mostly plant-based"),
    FLEXITARIAN("Flexitarian"),
    WHOLE_FOOD_PLANT_BASED("Whole-food plant-based"),
    HIGH_PROTEIN("High-protein"),
    BUDGET_FRIENDLY("Budget-friendly"),
    GLUTEN_FREE("Gluten-free"),
    NUT_FREE("Nut-free"),
    SOY_FREE("Soy-free"),
    QUICK_MEALS("Quick meals");
}

@Serializable
data class RecipeMacros(
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val fiberGrams: Int,
)

@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val prepMinutes: Int,
    val cookMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val mealType: String,
    val macros: RecipeMacros,
    val dietLabels: List<RecipeDietLabel>,
    val tags: List<String>,
    val ingredients: List<String>,
    val steps: List<String>,
    val allergens: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val imageUrl: String? = null,
) {
    val totalMinutes: Int get() = prepMinutes + cookMinutes
}
