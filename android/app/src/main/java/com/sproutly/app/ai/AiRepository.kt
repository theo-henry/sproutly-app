package com.sproutly.app.ai

import com.sproutly.app.core.result.AppResult

/**
 * Front for AI features (meal plan generation, shopping list, label analysis,
 * recipe-from-pantry, nearby recommendations, nutrition summaries).
 *
 * IMPORTANT: Do not embed Gemini/private keys in the Android client. The real
 * implementation must call a Supabase Edge Function or other backend that holds
 * the key server-side.
 */
interface AiRepository {
    suspend fun generateMealPlan(prompt: String): AppResult<String>
    suspend fun analyzeLabel(text: String): AppResult<String>
    suspend fun generateRecipeFromPantry(items: List<String>): AppResult<String>
    suspend fun shoppingListFromPlan(mealPlanJson: String): AppResult<String>
}

class StubAiRepository : AiRepository {
    override suspend fun generateMealPlan(prompt: String): AppResult<String> =
        AppResult.Failure("AI backend not yet configured")
    override suspend fun analyzeLabel(text: String): AppResult<String> =
        AppResult.Failure("AI backend not yet configured")
    override suspend fun generateRecipeFromPantry(items: List<String>): AppResult<String> =
        AppResult.Failure("AI backend not yet configured")
    override suspend fun shoppingListFromPlan(mealPlanJson: String): AppResult<String> =
        AppResult.Failure("AI backend not yet configured")
}
