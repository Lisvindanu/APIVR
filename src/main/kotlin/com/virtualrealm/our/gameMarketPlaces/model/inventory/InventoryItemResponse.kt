package com.virtualrealm.our.gameMarketPlaces.model.inventory

data class InventoryItemResponse(
    val itemId: Long,
    val name: String,
    val quantity: Int,
    val lastUpdated: String,
    val imageUrl: String?
)
