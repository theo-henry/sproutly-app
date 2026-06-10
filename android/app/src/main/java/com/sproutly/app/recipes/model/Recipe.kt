package com.sproutly.app.recipes.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val prepMinutes: Int? = null,
    val proteinGrams: Int? = null,
    val tags: List<String> = emptyList(),
)
