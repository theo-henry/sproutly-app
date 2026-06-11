package com.sproutly.app.mealplan.data

import com.sproutly.app.core.network.SupabaseClientProvider
import com.sproutly.app.core.result.AppResult
import com.sproutly.app.mealplan.model.MealPlan
import com.sproutly.app.mealplan.model.MealPlanFactory
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MealPlanRepository {
    private val client get() = SupabaseClientProvider.client
    private val table get() = client.postgrest.from("meal_plans")
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class Row(
        val id: String? = null,
        @SerialName("user_id") val userId: String,
        @SerialName("week_start") val weekStart: String,
        val days: List<com.sproutly.app.mealplan.model.MealPlanDay>,
    )

    suspend fun getForWeek(weekStartISO: String): AppResult<MealPlan?> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return AppResult.Success(null)
        val row = table.select {
            filter {
                eq("user_id", userId)
                eq("week_start", weekStartISO)
            }
            limit(1)
        }.decodeSingleOrNull<Row>()
        row?.let { MealPlan(weekStartISO = it.weekStart, days = it.days) }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Failure(it.message ?: "Failed to load meal plan", it) }
    )

    suspend fun upsert(plan: MealPlan): AppResult<MealPlan> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("Not signed in")
        val payload = Row(userId = userId, weekStart = plan.weekStartISO, days = plan.days)
        table.upsert(payload) {
            onConflict = "user_id,week_start"
            select()
        }.decodeSingle<Row>()
        plan
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Failure(it.message ?: "Failed to save meal plan", it) }
    )

    suspend fun loadOrCreate(weekStartISO: String = MealPlanFactory.currentMonday().toString()): MealPlan =
        when (val r = getForWeek(weekStartISO)) {
            is AppResult.Success -> r.data ?: MealPlanFactory.emptyPlan()
            is AppResult.Failure -> MealPlanFactory.emptyPlan()
        }
}
