package com.virtualrealm.our.gameMarketPlaces.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "username", unique = true)
    var username: String,

    @Column(name = "fullname")
    var fullname: String? = null,

    @Column(name = "email")
    @field:NotBlank
    val email: String,

    @Column(name = "password")
    var password: String,

    @Column(name = "googleId")
    val googleId: String? = null,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "isOtpVerified")
    var isOtpVerified: Boolean? = false,

    @Column(name = "role")
    var role: String? = "USER",

    @Column(name = "uuid")
    var uuid: String = java.util.UUID.randomUUID().toString(),

    @Column(name = "alamat")
    var address: String? = null,

    @Column(name = "nomerHp")
    var phoneNumber: String? = null,

    @Column(name = "google_token")
    var googleToken: String? = null,

    @Column(name = "google_refresh_token")
    var googleRefreshToken: String? = null
)
