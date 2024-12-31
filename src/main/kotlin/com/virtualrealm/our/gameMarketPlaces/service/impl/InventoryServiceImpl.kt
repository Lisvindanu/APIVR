package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.model.inventory.GetUserInventoryItemResponse
import com.virtualrealm.our.gameMarketPlaces.model.inventory.InventoryItemResponse
import com.virtualrealm.our.gameMarketPlaces.model.inventory.UseInventoryItemRequest
import com.virtualrealm.our.gameMarketPlaces.repository.InventoryRepository
import com.virtualrealm.our.gameMarketPlaces.service.InventoryServices
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class InventoryServiceImpl(
    private val inventoryRepository: InventoryRepository
): InventoryServices {

    override fun getUserInventory(userId: Long): GetUserInventoryItemResponse {
        val inventoryItems = inventoryRepository.findByUserId(userId).map {
            InventoryItemResponse(
                itemId = it.product.id ?: -1,  // Menggunakan ID produk dari entitas Product
                name = it.product.name,       // Menggunakan nama produk dari entitas Product
                quantity = it.quantity,
                lastUpdated = DateTimeFormatter.ISO_INSTANT.format(it.lastUpdated.toInstant()),  // Format tanggal yang konsisten
                imageUrl = it.product.imageUrl // Add image URL here
            )
        }
        println("Fetching inventory for userId: $userId")
        return GetUserInventoryItemResponse(
            code = 200,
            status = "success",
            data = inventoryItems
        )
    }


    override fun useInventoryItem(request: UseInventoryItemRequest): GetUserInventoryItemResponse {
        // Validasi input
        if (request.userId <= 0 || request.itemId <= 0 || request.quantity <= 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid input: userId, itemId, and quantity must be positive numbers"
            )
        }

        // Logika utama
        val inventoryItem = inventoryRepository.findByUserIdAndProductId(request.userId, request.itemId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Item with ID ${request.itemId} not found in user's inventory"
            )

        if (inventoryItem.quantity < request.quantity) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Insufficient quantity. Available: ${inventoryItem.quantity}, Requested: ${request.quantity}"
            )
        }

        // Update quantity
        inventoryItem.quantity -= request.quantity
        inventoryItem.lastUpdated = Date()
        inventoryRepository.save(inventoryItem)

        val responseData = InventoryItemResponse(
            itemId = inventoryItem.product.id ?: -1,
            name = inventoryItem.product.name,
            quantity = inventoryItem.quantity,
            lastUpdated = DateTimeFormatter.ISO_INSTANT.format(inventoryItem.lastUpdated.toInstant()),
            imageUrl = inventoryItem.product.imageUrl
        )

        return GetUserInventoryItemResponse(
            code = 200,
            status = "success",
            data = listOf(responseData)
        )
    }


    override fun updateInventoryAfterPurchase(userId: Long, itemId: Long, quantity: Int): Boolean {
        val inventory = inventoryRepository.findByUserIdAndProductId(userId, itemId)
        if (inventory != null && inventory.quantity >= quantity) {
            inventory.quantity -= quantity
            inventoryRepository.save(inventory)
            return true
        }
        return false
    }
}
