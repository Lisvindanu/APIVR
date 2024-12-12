package com.virtualrealm.our.gameMarketPlaces.model.authModel

data class LoginRequest(
    val email: String,
    val password: String? = null,
    val username: String? = null,
    val otp: String? = null,
    val isGoogle: Boolean = false,  // Flag for Google registration
    val googleToken: String? = null, // Google OAuth token


)
