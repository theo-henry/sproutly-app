package com.sproutly.app.auth.data

import android.content.Context
import android.content.SharedPreferences
import com.sproutly.app.mealplan.model.MealPlan
import com.sproutly.app.profile.model.DietPreference
import com.sproutly.app.profile.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DemoAccountStore {
    private const val PREFS_NAME = "sproutly_demo_account"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_PROFILE = "profile"
    private const val KEY_MEAL_PLANS = "meal_plans"

    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var prefs: SharedPreferences
    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _enabled.value = prefs.getBoolean(KEY_ENABLED, false)
    }

    fun isEnabled(): Boolean = enabled.value

    fun enable() {
        requireInitialized()
        prefs.edit()
            .putBoolean(KEY_ENABLED, true)
            .putString(KEY_PROFILE, json.encodeToString(defaultProfile()))
            .remove(KEY_MEAL_PLANS)
            .apply()
        _enabled.value = true
    }

    fun clear() {
        requireInitialized()
        prefs.edit().clear().apply()
        _enabled.value = false
    }

    fun getProfile(): Profile {
        requireInitialized()
        val encoded = prefs.getString(KEY_PROFILE, null) ?: return defaultProfile()
        return runCatching { json.decodeFromString<Profile>(encoded) }.getOrElse { defaultProfile() }
    }

    fun saveProfile(profile: Profile): Profile {
        requireInitialized()
        val demoProfile = profile.copy(
            id = DEMO_ID,
            email = DEMO_EMAIL,
            dietPreference = profile.dietPreference ?: DietPreference.VEGETARIAN.value,
            isDemo = true,
        )
        prefs.edit().putString(KEY_PROFILE, json.encodeToString(demoProfile)).apply()
        return demoProfile
    }

    fun getMealPlan(weekStartISO: String): MealPlan? = mealPlans()[weekStartISO]

    fun saveMealPlan(plan: MealPlan): MealPlan {
        val next = mealPlans().toMutableMap()
        next[plan.weekStartISO] = plan
        prefs.edit().putString(KEY_MEAL_PLANS, json.encodeToString(next)).apply()
        return plan
    }

    private fun mealPlans(): Map<String, MealPlan> {
        requireInitialized()
        val encoded = prefs.getString(KEY_MEAL_PLANS, null) ?: return emptyMap()
        return runCatching { json.decodeFromString<Map<String, MealPlan>>(encoded) }.getOrElse { emptyMap() }
    }

    private fun defaultProfile(): Profile = Profile(
        id = DEMO_ID,
        email = DEMO_EMAIL,
        displayName = "Demo Account",
        dietPreference = DietPreference.VEGETARIAN.value,
        isDemo = true,
    )

    private fun requireInitialized() {
        check(::prefs.isInitialized) { "DemoAccountStore.init() not called" }
    }

    const val DEMO_ID = "demo"
    const val DEMO_EMAIL = "demo@sproutly.local"
}
