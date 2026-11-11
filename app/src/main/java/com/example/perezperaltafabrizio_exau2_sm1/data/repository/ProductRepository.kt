package com.example.perezperaltafabrizio_exau2_sm1.data.repository

import com.example.perezperaltafabrizio_exau2_sm1.data.local.ProductDao
import com.example.perezperaltafabrizio_exau2_sm1.data.local.ProductEntity
import com.example.perezperaltafabrizio_exau2_sm1.data.remote.FakeStoreApi
import com.example.perezperaltafabrizio_exau2_sm1.data.remote.ProductDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProductRepository(
    private val productDao: ProductDao,
    private val api: FakeStoreApi
) {
    /* Busqueda y lectura local desde Room (Fuente Unica de la Verdad) */
    fun getLocalProducts(query: String, category: String): Flow<List<ProductEntity>> {
        val cleanQuery = query.trim()
        val isAllCategories = category == "Todas"

        return when {
            cleanQuery.isEmpty() && isAllCategories -> productDao.getAllProducts()
            cleanQuery.isNotEmpty() && isAllCategories -> productDao.getProductsByQuery(cleanQuery)
            cleanQuery.isEmpty() -> productDao.getProductsByCategory(category)
            else -> productDao.getProductsByQueryAndCategory(cleanQuery, category)
        }
    }

    fun getAllCategories(): Flow<List<String>> {
        return productDao.getAllCategories()
    }

    fun getProductById(productId: Int): Flow<ProductEntity?> {
        return productDao.getProductById(productId)
    }

    /* Obtener catalogo de FakeStore API y guardarlo en Room */
    suspend fun refreshProductsFromApi() = withContext(Dispatchers.IO) {
        val response = api.getProducts()
        val currentTime = System.currentTimeMillis()

        val entities = response.mapNotNull { dto ->
            if (dto.title.isBlank()) return@mapNotNull null

            val existingProduct = productDao.getProductByIdOnce(dto.id)
            val savedStock = existingProduct?.stock ?: DEFAULT_STOCK

            mapToEntity(dto = dto, stock = savedStock, lastUpdated = currentTime)
        }

        if (entities.isNotEmpty()) {
            productDao.insertProducts(entities)
        }
    }

    /* Sincronizar un producto especifico manteniendo su stock local */
    suspend fun refreshProductById(productId: Int) = withContext(Dispatchers.IO) {
        val dto = api.getProductById(productId)
        val existingProduct = productDao.getProductByIdOnce(productId)
        val savedStock = existingProduct?.stock ?: DEFAULT_STOCK
        val entity = mapToEntity(
            dto = dto,
            stock = savedStock,
            lastUpdated = System.currentTimeMillis()
        )
        productDao.insertProduct(entity)
    }

    /* Gestion local de inventario en Room (funciona offline y online) */
    suspend fun updateStock(productId: Int, newStock: Int) = withContext(Dispatchers.IO) {
        if (newStock >= 0) {
            productDao.updateStock(productId, newStock)
        }
    }

    private fun mapToEntity(
        dto: ProductDto,
        stock: Int,
        lastUpdated: Long
    ): ProductEntity {
        return ProductEntity(
            id = dto.id,
            title = dto.title,
            price = dto.price,
            description = dto.description,
            category = dto.category,
            image = dto.image,
            ratingRate = dto.rating?.rate ?: 0.0,
            ratingCount = dto.rating?.count ?: 0,
            stock = stock,
            lastUpdated = lastUpdated
        )
    }

    companion object {
        private const val DEFAULT_STOCK = 10
    }
}
