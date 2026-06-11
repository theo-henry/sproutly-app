package com.sproutly.app.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sproutly.app.products.data.CartRepository
import com.sproutly.app.products.data.ProductRepository
import com.sproutly.app.products.model.CartSummary
import com.sproutly.app.products.model.Product
import com.sproutly.app.products.model.ProductCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductsUiState(
    val categories: List<ProductCategory> = ProductCategory.entries,
    val selectedCategory: ProductCategory = ProductCategory.ALL,
    val query: String = "",
    val products: List<Product> = emptyList(),
    val visibleProducts: List<Product> = emptyList(),
    val cartQuantities: Map<String, Int> = emptyMap(),
    val cartSummary: CartSummary = CartSummary(),
    val loading: Boolean = true,
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val productRepo = ProductRepository()
    private val cartRepo = CartRepository(application.applicationContext)

    private val _state = MutableStateFlow(ProductsUiState())
    val state: StateFlow<ProductsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val products = productRepo.products()
            _state.value = _state.value.copy(
                products = products,
                visibleProducts = productRepo.filteredProducts(
                    products = products,
                    category = _state.value.selectedCategory,
                    query = _state.value.query,
                ),
                cartSummary = summarizeCart(_state.value.cartQuantities, products),
                loading = false,
            )
        }

        viewModelScope.launch {
            cartRepo.cartLines.collect { lines ->
                val quantities = lines.associate { it.productId to it.quantity }
                _state.value = _state.value.copy(
                    cartQuantities = quantities,
                    cartSummary = summarizeCart(quantities, _state.value.products),
                )
            }
        }
    }

    fun selectCategory(category: ProductCategory) {
        _state.value = _state.value.copy(selectedCategory = category)
        applyFilters()
    }

    fun updateQuery(query: String) {
        _state.value = _state.value.copy(query = query)
        applyFilters()
    }

    fun addToCart(productId: String) {
        viewModelScope.launch { cartRepo.add(productId) }
    }

    fun decrementCart(productId: String) {
        viewModelScope.launch { cartRepo.decrement(productId) }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch { cartRepo.remove(productId) }
    }

    fun clearCart() {
        viewModelScope.launch { cartRepo.clear() }
    }

    private fun applyFilters() {
        val current = _state.value
        _state.value = current.copy(
            visibleProducts = productRepo.filteredProducts(
                products = current.products,
                category = current.selectedCategory,
                query = current.query,
            )
        )
    }

    private fun summarizeCart(quantities: Map<String, Int>, products: List<Product>): CartSummary {
        val productById = products.associateBy { it.id }
        val totalItems = quantities.values.sum()
        val estimatedTotal = quantities.entries.sumOf { (productId, quantity) ->
            (productById[productId]?.priceEur ?: 0.0) * quantity
        }

        return CartSummary(
            totalItems = totalItems,
            estimatedTotalEur = estimatedTotal,
        )
    }
}
