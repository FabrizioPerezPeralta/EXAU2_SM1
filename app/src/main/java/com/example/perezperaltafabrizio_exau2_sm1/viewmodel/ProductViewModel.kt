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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class ProductViewModel(
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState

    private var localDataJob: Job? = null

    init {
        observeCategories()
        observeLocalDatabase(_uiState.value.searchQuery, _uiState.value.selectedCategory)
        fetchRemoteData()
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        observeLocalDatabase(newQuery, _uiState.value.selectedCategory)
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        observeLocalDatabase(_uiState.value.searchQuery, category)
    }

    fun performSearch() {
        val cleanQuery = _uiState.value.searchQuery.trim()
        _uiState.update {
            it.copy(
                searchQuery = cleanQuery,
                errorMessage = null
            )
        }
        observeLocalDatabase(cleanQuery, _uiState.value.selectedCategory)
        fetchRemoteData()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collectLatest { categoryList ->
                _uiState.update { it.copy(categories = categoryList) }
            }
        }
    }

    private fun observeLocalDatabase(query: String, category: String) {
        localDataJob?.cancel()
        localDataJob = viewModelScope.launch {
            repository.getLocalProducts(query, category).collectLatest { productList ->
                _uiState.update { currentState ->
                    currentState.copy(products = productList)
                }
            }
        }
    }

    fun fetchRemoteData() {
        val context = getApplication<Application>().applicationContext
        val isOnline = NetworkUtils.isNetworkAvailable(context)

        _uiState.update { it.copy(isOffline = !isOnline) }

        if (!isOnline) {
            _uiState.update { currentState ->
                val errorMsg = if (currentState.products.isEmpty()) {
                    "Sin conexion a Internet. No hay productos guardados en el dispositivo."
                } else {
                    "Sin conexion a Internet. Mostrando datos locales."
                }
                currentState.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.refreshProductsFromApi()
                _uiState.update { it.copy(isLoading = false, isOffline = false) }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    val isConnIssue = e is IOException
                    val errorMsg = if (isConnIssue) {
                        "Error de red al consultar FakeStore API. Mostrando datos locales."
                    } else {
                        "Error al cargar catalogo: ${e.localizedMessage ?: "Desconocido"}"
                    }
                    currentState.copy(
                        isLoading = false,
                        isOffline = isConnIssue,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getDatabase(application)
            val repository = ProductRepository(db.productDao(), RetrofitClient.api)
            return ProductViewModel(application, repository) as T
        }
    }
}

