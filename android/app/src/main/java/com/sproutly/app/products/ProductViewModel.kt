package com.sproutly.app.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sproutly.app.products.data.ProductRepository
import com.sproutly.app.products.model.Deal
import com.sproutly.app.products.model.ProductCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductsUiState(
    val categories: List<ProductCategory> = emptyList(),
    val selectedCategory: ProductCategory = ProductCategory.ALL,
    val deals: List<Deal> = emptyList(),
)

class ProductViewModel(
    private val repo: ProductRepository = ProductRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(ProductsUiState())
    val state: StateFlow<ProductsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = ProductsUiState(
                categories = repo.categories(),
                deals = repo.deals(),
            )
        }
    }

    fun selectCategory(c: ProductCategory) {
        _state.value = _state.value.copy(selectedCategory = c)
    }
}
