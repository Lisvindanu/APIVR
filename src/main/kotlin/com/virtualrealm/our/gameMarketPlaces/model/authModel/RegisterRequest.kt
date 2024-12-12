package com.virtualrealm.our.gameMarketPlaces.model.authModel

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val isGoogle: Boolean = false,  // Flag for Google registration
    val googleToken: String? = null, // Google OAuth token
    val isOtpVerified: Boolean = false

)
