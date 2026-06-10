package com.sproutly.app.shopping

import kotlinx.serialization.Serializable

/**
 * Placeholder shopping list model. Future: derive from meal plan via AI, sync to Supabase.
 */
@Serializable
data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: String? = null,
    val checked: Boolean = false,
)

@Serializable
data class ShoppingList(
    val id: String,
    val items: List<ShoppingItem> = emptyList(),
)
