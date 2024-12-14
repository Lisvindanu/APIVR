package com.virtualrealm.our.gameMarketPlaces.entity

import jakarta.persistence.*

@Entity
@Table(name = "genres")
data class Genre(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false, unique = true)
    var name: String,

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category
)
