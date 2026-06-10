package com.sproutly.app.products.data

import com.sproutly.app.products.model.Deal
import com.sproutly.app.products.model.Product
import com.sproutly.app.products.model.ProductCategory

/**
 * Placeholder repository. Future: real catalog, barcode lookup, nutrition flags,
 * allergens, vegan status, better alternatives.
 */
class ProductRepository {
    suspend fun categories(): List<ProductCategory> = ProductCategory.entries

    suspend fun deals(): List<Deal> = listOf(
        Deal(Product("d1", "Oat milk barista", 2.49, 25, category = ProductCategory.DRINKS.name), "Best for foam"),
        Deal(Product("d2", "Tempeh block", 3.20, 15, category = ProductCategory.PANTRY.name), "21g protein/100g"),
        Deal(Product("d3", "Vegan cheese", 4.10, 10, category = ProductCategory.DAIRY_FREE.name), "Cashew base"),
        Deal(Product("d4", "Lentil pasta", 1.90, 20, category = ProductCategory.PANTRY.name), "13g protein/serving"),
    )

    // TODO: searchByQuery(), lookupByBarcode(code), saveFavorite(id), suggestAlternatives(productId)
}
