package com.sproutly.app.products.model

import kotlinx.serialization.Serializable

enum class ProductCategory(val label: String) {
    ALL("All"),
    PANTRY("Pantry"),
    DAIRY_FREE("Dairy-free"),
    PROTEIN("Protein"),
    SNACKS("Snacks"),
    FROZEN("Frozen"),
    DRINKS("Drinks");
}

@Serializable
enum class DietLabel(val label: String) {
    VEGAN("Vegan"),
    VEGETARIAN("Vegetarian");
}

@Serializable
data class StoreHint(
    val id: String,
    val name: String,
    val area: String,
    val note: String,
)

@Serializable
data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: ProductCategory,
    val priceEur: Double,
    val dietLabel: DietLabel,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val nutritionFlags: List<String> = emptyList(),
    val storeHints: List<StoreHint> = emptyList(),
    val featured: Boolean = false,
    val sourceAttribution: String? = null,
)

@Serializable
data class CartLine(
    val productId: String,
    val quantity: Int,
)

data class CartSummary(
    val totalItems: Int = 0,
    val estimatedTotalEur: Double = 0.0,
)
