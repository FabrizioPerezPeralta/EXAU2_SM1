package com.example.perezperaltafabrizio_exau2_sm1.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val ratingRate: Double,
    val ratingCount: Int,
    val stock: Int = 10,
    val lastUpdated: Long = System.currentTimeMillis()
)
