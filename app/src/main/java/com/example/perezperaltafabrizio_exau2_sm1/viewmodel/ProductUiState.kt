package com.example.perezperaltafabrizio_exau2_sm1.viewmodel

import com.example.perezperaltafabrizio_exau2_sm1.data.local.ProductEntity

data class ProductUiState(
    val searchQuery: String = "",
    val selectedCategory: String = ALL_CATEGORIES,
    val categories: List<String> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        const val ALL_CATEGORIES = "Todas"
    }
}

