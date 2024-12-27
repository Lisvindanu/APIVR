package com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel

import com.virtualrealm.our.gameMarketPlaces.model.genre.GenreResponse

data class ProductResponse(
    val id: Long?,
    val name: String?,
    val description: String?,
    val specifications: String?,
    val price: Long?,
    val quantity: Int?,
    val categoryId: Long?,
    val categoryName: String?,
    val genres: List<GenreResponse>,
    val created_at: String,
    val updated_at: String,
    val imageUrl: String?
)