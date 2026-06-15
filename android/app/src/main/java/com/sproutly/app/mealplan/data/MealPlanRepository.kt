package com.sproutly.app.mealplan.data

import com.sproutly.app.auth.data.DemoAccountStore
import com.sproutly.app.core.config.AppConfig
import com.sproutly.app.core.network.SupabaseClientProvider
import com.sproutly.app.core.result.AppResult
import com.sproutly.app.mealplan.model.MealPlan
import com.sproutly.app.mealplan.model.MealPlanFactory
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MealPlanRepository {
    private val client get() = SupabaseClientProvider.client
    private val table get() = client.postgrest.from("meal_plans")
    private val http = HttpClient(OkHttp)
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class Row(
        val id: String? = null,
        @SerialName("user_id") val userId: String,
        @SerialName("week_start") val weekStart: String,
        val days: List<com.sproutly.app.mealplan.model.MealPlanDay>,
    )

    @Serializable
    private data class GenerateRequest(
        @SerialName("week_start") val weekStart: String,
    )

    @Serializable
    private data class ErrorResponse(val error: String? = null)

    suspend fun getForWeek(weekStartISO: String): AppResult<MealPlan?> = runCatching {
        if (DemoAccountStore.isEnabled()) return AppResult.Success(DemoAccountStore.getMealPlan(weekStartISO))
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
        if (DemoAccountStore.isEnabled()) return AppResult.Success(DemoAccountStore.saveMealPlan(plan))
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

    suspend fun requestGeneratedPlan(weekStartISO: String): AppResult<MealPlan> = runCatching {
        if (DemoAccountStore.isEnabled()) error("Meal plan generation is unavailable in demo mode.")
        val accessToken = client.auth.currentAccessTokenOrNull() ?: error("Not signed in")
        val functionUrl = "${AppConfig.supabaseUrl.trimEnd('/')}/functions/v1/request-meal-plan"
        val response = http.post(functionUrl) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(GenerateRequest(weekStartISO)))
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.bodyAsText() }.getOrNull()
            val message = body
                ?.let { runCatching { json.decodeFromString<ErrorResponse>(it).error }.getOrNull() }
                ?: body
                ?: "Meal plan generation request failed."
            error(message)
        }

        json.decodeFromString<MealPlan>(response.bodyAsText())
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Failure(it.message ?: "Failed to request meal plan", it) }
    )

    suspend fun loadOrCreate(weekStartISO: String = MealPlanFactory.currentMonday().toString()): MealPlan =
        when (val r = getForWeek(weekStartISO)) {
            is AppResult.Success -> r.data ?: MealPlanFactory.emptyPlan()
            is AppResult.Failure -> MealPlanFactory.emptyPlan()
        }
}
