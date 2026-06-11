package com.sproutly.app.products.data

import com.sproutly.app.products.model.DietLabel
import com.sproutly.app.products.model.Product
import com.sproutly.app.products.model.ProductCategory
import com.sproutly.app.products.model.StoreHint

class ProductRepository {
    private val stores = listOf(
        StoreHint(
            id = "mercadona-centro",
            name = "Mercadona",
            area = "Centro / Chamberi",
            note = "Good for staples and plant milks",
        ),
        StoreHint(
            id = "carrefour-market",
            name = "Carrefour Market",
            area = "Malasana / Salamanca",
            note = "Good range for dairy-free and frozen",
        ),
        StoreHint(
            id = "bio-c-bon",
            name = "Bio c' Bon",
            area = "Centro",
            note = "Likely for organic pantry and snacks",
        ),
        StoreHint(
            id = "aldi-madrid",
            name = "ALDI",
            area = "Madrid city",
            note = "Budget-friendly plant-based basics",
        ),
        StoreHint(
            id = "lidl-madrid",
            name = "Lidl",
            area = "Madrid city",
            note = "Good place to check for vegan frozen items",
        ),
    )

    suspend fun categories(): List<ProductCategory> = ProductCategory.entries

    suspend fun products(): List<Product> = catalog

    fun filteredProducts(
        products: List<Product>,
        category: ProductCategory,
        query: String,
    ): List<Product> {
        val normalizedQuery = query.trim().lowercase()
        return products.filter { product ->
            val categoryMatches = category == ProductCategory.ALL || product.category == category
            val queryMatches = normalizedQuery.isBlank() ||
                product.name.lowercase().contains(normalizedQuery) ||
                product.brand.lowercase().contains(normalizedQuery) ||
                product.tags.any { it.lowercase().contains(normalizedQuery) } ||
                product.storeHints.any { it.name.lowercase().contains(normalizedQuery) }

            categoryMatches && queryMatches
        }
    }

    private fun store(id: String): StoreHint = stores.first { it.id == id }

    private val catalog = listOf(
        Product(
            id = "oatly-barista",
            name = "Barista oat drink",
            brand = "Oatly style",
            category = ProductCategory.DRINKS,
            priceEur = 2.49,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=900&q=80",
            tags = listOf("coffee", "breakfast", "calcium"),
            nutritionFlags = listOf("Fortified", "No dairy"),
            storeHints = listOf(store("carrefour-market"), store("mercadona-centro")),
            featured = true,
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "sojasun-yogurt",
            name = "Soy yogurt multipack",
            brand = "Soy dairy-free",
            category = ProductCategory.DAIRY_FREE,
            priceEur = 2.95,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=900&q=80",
            tags = listOf("breakfast", "snack", "soy"),
            allergens = listOf("Soy"),
            nutritionFlags = listOf("Protein source"),
            storeHints = listOf(store("carrefour-market"), store("bio-c-bon")),
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "firm-tofu",
            name = "Firm tofu block",
            brand = "Plant kitchen",
            category = ProductCategory.PROTEIN,
            priceEur = 2.35,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1627959745251-7cbe1f96c0c6?auto=format&fit=crop&w=900&q=80",
            tags = listOf("protein", "stir-fry", "soy"),
            allergens = listOf("Soy"),
            nutritionFlags = listOf("High protein"),
            storeHints = listOf(store("mercadona-centro"), store("bio-c-bon")),
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "smoked-tempeh",
            name = "Smoked tempeh",
            brand = "Veg protein",
            category = ProductCategory.PROTEIN,
            priceEur = 3.75,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?auto=format&fit=crop&w=900&q=80",
            tags = listOf("protein", "fermented", "meal prep"),
            allergens = listOf("Soy"),
            nutritionFlags = listOf("High protein", "Fermented"),
            storeHints = listOf(store("bio-c-bon"), store("carrefour-market")),
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "red-lentil-pasta",
            name = "Red lentil pasta",
            brand = "Pantry boost",
            category = ProductCategory.PANTRY,
            priceEur = 1.95,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1551462147-37885acc36f1?auto=format&fit=crop&w=900&q=80",
            tags = listOf("pasta", "protein", "quick meals"),
            nutritionFlags = listOf("High protein", "Gluten-free"),
            storeHints = listOf(store("aldi-madrid"), store("mercadona-centro")),
            featured = true,
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "vegan-cheese-slices",
            name = "Vegan cheese slices",
            brand = "Dairy-free melt",
            category = ProductCategory.DAIRY_FREE,
            priceEur = 3.65,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1552767059-ce182ead6c1b?auto=format&fit=crop&w=900&q=80",
            tags = listOf("sandwich", "dairy-free", "melt"),
            allergens = listOf("May contain nuts"),
            nutritionFlags = listOf("No dairy"),
            storeHints = listOf(store("carrefour-market"), store("lidl-madrid")),
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "classic-hummus",
            name = "Classic hummus",
            brand = "Fresh mezze",
            category = ProductCategory.SNACKS,
            priceEur = 1.80,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1577805947697-89e18249d767?auto=format&fit=crop&w=900&q=80",
            tags = listOf("snack", "chickpea", "lunch"),
            allergens = listOf("Sesame"),
            nutritionFlags = listOf("Fiber source"),
            storeHints = listOf(store("mercadona-centro"), store("carrefour-market")),
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "chickpea-crisps",
            name = "Chickpea protein crisps",
            brand = "Crunch veg",
            category = ProductCategory.SNACKS,
            priceEur = 1.65,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1621939514649-280e2ee25f60?auto=format&fit=crop&w=900&q=80",
            tags = listOf("snack", "protein", "high fiber"),
            nutritionFlags = listOf("High fiber"),
            storeHints = listOf(store("bio-c-bon"), store("aldi-madrid")),
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "veggie-burgers",
            name = "Veggie burger patties",
            brand = "Garden grill",
            category = ProductCategory.FROZEN,
            priceEur = 3.90,
            dietLabel = DietLabel.VEGETARIAN,
            imageUrl = "https://images.unsplash.com/photo-1520072959219-c595dc870360?auto=format&fit=crop&w=900&q=80",
            tags = listOf("burger", "dinner", "frozen"),
            allergens = listOf("May contain egg"),
            nutritionFlags = listOf("Source of protein"),
            storeHints = listOf(store("lidl-madrid"), store("carrefour-market")),
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "frozen-falafel",
            name = "Frozen falafel",
            brand = "Mediterranean box",
            category = ProductCategory.FROZEN,
            priceEur = 2.80,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1593001872095-7d5b3868fb1d?auto=format&fit=crop&w=900&q=80",
            tags = listOf("falafel", "freezer", "quick meals"),
            nutritionFlags = listOf("Fiber source"),
            storeHints = listOf(store("lidl-madrid"), store("bio-c-bon")),
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "soy-chocolate-dessert",
            name = "Dark chocolate soy dessert",
            brand = "Plant sweet",
            category = ProductCategory.DAIRY_FREE,
            priceEur = 2.25,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=900&q=80",
            tags = listOf("dessert", "soy", "chocolate"),
            allergens = listOf("Soy"),
            nutritionFlags = listOf("No dairy"),
            storeHints = listOf(store("carrefour-market"), store("mercadona-centro")),
            sourceAttribution = "Product image: Unsplash",
        ),
        Product(
            id = "pea-protein-drink",
            name = "Pea protein shake",
            brand = "Active plant",
            category = ProductCategory.DRINKS,
            priceEur = 2.10,
            dietLabel = DietLabel.VEGAN,
            imageUrl = "https://images.unsplash.com/photo-1553530666-ba11a90bb0ae?auto=format&fit=crop&w=900&q=80",
            tags = listOf("protein", "gym", "ready to drink"),
            nutritionFlags = listOf("20g protein"),
            storeHints = listOf(store("aldi-madrid"), store("carrefour-market")),
            sourceAttribution = "Product image: Unsplash",
        ),
    )
}
