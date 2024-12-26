package com.virtualrealm.our.gameMarketPlaces.model.authModel

data class RegisterRequest(
    val username: String,
    val fullname: String, // Add this
    val email: String,
    val password: String,
    val password_confirmation: String,
    val imageUrl: String? = null, // Add this
    val isGoogle: Boolean = false,
    val googleToken: String? = null,
    val isOtpVerified: Boolean = false
)
