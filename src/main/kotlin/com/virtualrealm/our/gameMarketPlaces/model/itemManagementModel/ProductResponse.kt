package com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel

data class ProductResponse(
    val id: Long?,
    val name: String?,
    val price: Long?,
    val quantity: Int?,
    val categoryId: Long?,
    val categoryName: String?, // Tambahkan nama kategori
    val genreId: Long?,
    val genreName: String?, // Tambahkan nama genre (opsional)
    val created_at: String,
    val updated_at: String,
    val imageUrl: String?
)
