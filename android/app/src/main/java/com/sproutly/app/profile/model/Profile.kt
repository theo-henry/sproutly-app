package com.sproutly.app.profile.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class DietPreference(val value: String, val label: String) {
    VEGAN("vegan", "Vegan"),
    VEGETARIAN("vegetarian", "Vegetarian"),
    MOSTLY_PLANT_BASED("mostly_plant_based", "Mostly plant-based"),
    FLEXITARIAN("flexitarian", "Flexitarian"),
    WHOLE_FOOD_PLANT_BASED("whole_food_plant_based", "Whole-food plant-based"),
    OTHER("other", "Other");

    companion object {
        fun fromValue(value: String?): DietPreference? = entries.firstOrNull { it.value == value }
    }
}

object DietTags {
    val ALL = listOf(
        "High-protein",
        "Budget-friendly",
        "Gluten-free",
        "Nut-free",
        "Soy-free",
        "Quick meals",
    )
}

@Serializable
data class Profile(
    val id: String,
    val email: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_path") val avatarPath: String? = null,
    val city: String? = null,
    val country: String? = null,
    @SerialName("diet_preference") val dietPreference: String? = null,
    @SerialName("diet_tags") val dietTags: List<String> = emptyList(),
    @SerialName("is_demo") val isDemo: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
