package com.example.perezperaltafabrizio_exau2_sm1.data.remote

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("price") val price: Double,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("image") val image: String,
    @SerializedName("rating") val rating: RatingDto? = null
)

data class RatingDto(
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("count") val count: Int = 0
)
