package com.example.perezperaltafabrizio_exau2_sm1.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.perezperaltafabrizio_exau2_sm1.data.local.AppDatabase
import com.example.perezperaltafabrizio_exau2_sm1.data.remote.RetrofitClient
import com.example.perezperaltafabrizio_exau2_sm1.data.repository.ProductRepository
import com.example.perezperaltafabrizio_exau2_sm1.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val productId: Int,
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState

    init {
        observeProduct()
        refreshProductInBackground()
    }

    private fun observeProduct() {
        viewModelScope.launch {
            repository.getProductById(productId).collectLatest { product ->
                _uiState.update {
                    it.copy(
                        product = product,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun refreshProductInBackground() {
        val context = getApplication<Application>().applicationContext
        if (!NetworkUtils.isNetworkAvailable(context)) return

        viewModelScope.launch {
            try {
                repository.refreshProductById(productId)
            } catch (_: Exception) {
                // Si falla la red, Room sigue mostrando el detalle guardado localmente
            }
        }
    }

    fun increaseStock() {
        val currentStock = _uiState.value.product?.stock ?: return
        saveStock(currentStock + 1)
    }

    fun decreaseStock() {
        val currentStock = _uiState.value.product?.stock ?: return
        if (currentStock <= 0) return
        saveStock(currentStock - 1)
    }

    fun updateStockFromInput(stockText: String) {
        val parsed = stockText.trim().toIntOrNull()
        if (parsed == null || parsed < 0) {
            _uiState.update { it.copy(stockMessage = "Ingresa un numero entero valido (0 o mayor).") }
            return
        }
        saveStock(parsed)
    }

    fun clearStockMessage() {
        _uiState.update { it.copy(stockMessage = null) }
    }

    private fun saveStock(newStock: Int) {
        viewModelScope.launch {
            repository.updateStock(productId, newStock)
            _uiState.update { it.copy(stockMessage = "Inventario actualizado en Room.") }
        }
    }

    class Factory(
        private val productId: Int,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getDatabase(application)
            val repository = ProductRepository(db.productDao(), RetrofitClient.api)
            return ProductDetailViewModel(productId, application, repository) as T
        }
    }
}

