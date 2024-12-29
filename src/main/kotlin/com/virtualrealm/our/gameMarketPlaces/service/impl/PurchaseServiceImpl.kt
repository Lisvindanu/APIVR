package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.entity.Inventory
import com.virtualrealm.our.gameMarketPlaces.entity.Purchase
import com.virtualrealm.our.gameMarketPlaces.model.purchase.PurchaseRequest
import com.virtualrealm.our.gameMarketPlaces.repository.InventoryRepository
import com.virtualrealm.our.gameMarketPlaces.repository.ProductRepository
import com.virtualrealm.our.gameMarketPlaces.repository.PurchaseRepository
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import com.virtualrealm.our.gameMarketPlaces.service.PurchaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PurchaseServiceImpl @Autowired constructor(
    private val purchaseRepository: PurchaseRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val inventoryRepository: InventoryRepository  // Tambahkan ini
) : PurchaseService {

    @Transactional  // Tambahkan anotasi ini untuk memastikan atomicity
    override fun createPurchase(request: PurchaseRequest): Purchase {
        // Validasi dan membuat purchase seperti sebelumnya
        val product = productRepository.findById(request.productId)
            .orElseThrow { RuntimeException("Product not found") }
        val user = userRepository.findById(request.userId)
            .orElseThrow { RuntimeException("User not found") }

        // Cek stok produk
        if (product.quantity < request.quantity) {
            throw RuntimeException("Insufficient product stock")
        }

        // Kurangi stok produk
        product.quantity -= request.quantity
        productRepository.save(product)

        // Buat purchase record
        val totalPrice = product.price * request.quantity
        val purchase = Purchase(
            user = user,
            product = product,
            quantity = request.quantity,
            totalPrice = totalPrice
        )

        // Simpan purchase
        val savedPurchase = purchaseRepository.save(purchase)

        // Update atau buat inventory item
        val existingInventory = inventoryRepository.findByUserIdAndProductId(user.id!!, product.id!!)
        if (existingInventory != null) {
            // Update existing inventory
            existingInventory.quantity += request.quantity
            existingInventory.lastUpdated = Date()
            inventoryRepository.save(existingInventory)
        } else {
            // Create new inventory entry
            val newInventory = Inventory(
                user = user,
                product = product,
                quantity = request.quantity,
                lastUpdated = Date()
            )
            inventoryRepository.save(newInventory)
        }

        return savedPurchase
    }
    override fun getPurchaseHistory(userId: Long): List<Purchase> {
        return purchaseRepository.findByUserId(userId)
    }
}