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

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String?,

    @Column(name = "specifications", columnDefinition = "TEXT")
    var specifications: String?,

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "product_genres",
        joinColumns = [JoinColumn(name = "product_id")],
        inverseJoinColumns = [JoinColumn(name = "genre_id")]
    )
    var genres: MutableSet<Genre> = mutableSetOf(),

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "youtube_url")
var youtubeUrl: String? = null

)