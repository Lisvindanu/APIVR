package com.virtualrealm.our.gameMarketPlaces.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "price", nullable = false)
    var price: Long,

    @Column(name = "quantity", nullable = false)
    var quantity: Int,

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var createdAt: Date,

    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var updatedAt: Date,

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @ManyToOne
    @JoinColumn(name = "genre_id", nullable = true)
    var genre: Genre? = null, // Genre opsional, hanya berlaku jika kategori adalah "Game"

    @Column(name = "image_url")  // Optional field for storing image URL
    var imageUrl: String? = null
)
