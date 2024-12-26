package com.virtualrealm.our.gameMarketPlaces.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tokens")
data class UserToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "username", nullable = false)
    val username: String,

    @Column(unique = true, nullable = false)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "sub", unique = true)
    val sub: String? = null,


    @Column(name = "is_logged_in")  // Menambahkan status login
    var isLoggedIn: Boolean = true


)
