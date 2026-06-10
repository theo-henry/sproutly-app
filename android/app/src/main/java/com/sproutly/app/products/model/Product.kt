package com.sproutly.app.products.model

import kotlinx.serialization.Serializable

enum class ProductCategory(val label: String) {
    ALL("All"), PANTRY("Pantry"), DAIRY_FREE("Dairy-free"),
    SNACKS("Snacks"), FROZEN("Frozen"), DRINKS("Drinks");
}

@Serializable
data class Product(
    val id: String,
    val name: String,
    val priceEur: Double,
    val discountPercent: Int? = null,
    val isVegan: Boolean = true,
    val allergens: List<String> = emptyList(),
    val nutritionFlags: List<String> = emptyList(),
    val category: String = ProductCategory.PANTRY.name,
    val barcode: String? = null,
)

data class Deal(
    val product: Product,
    val highlight: String,
)
