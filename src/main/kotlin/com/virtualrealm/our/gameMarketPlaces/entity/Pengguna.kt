package com.virtualrealm.our.gameMarketPlaces.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pengguna")
data class Pengguna(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    var nama: String,

    @Column(nullable = false)
    var password: String,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)