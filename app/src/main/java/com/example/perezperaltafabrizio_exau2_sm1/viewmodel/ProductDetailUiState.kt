package com.example.perezperaltafabrizio_exau2_sm1.viewmodel

import com.example.perezperaltafabrizio_exau2_sm1.data.local.ProductEntity

data class ProductDetailUiState(
    val product: ProductEntity? = null,
    val isLoading: Boolean = true,
    val stockMessage: String? = null
)

