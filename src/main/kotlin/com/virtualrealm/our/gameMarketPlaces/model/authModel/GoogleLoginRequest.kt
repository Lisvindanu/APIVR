package com.virtualrealm.our.gameMarketPlaces.model.authModel

data class GoogleLoginRequest(
    val googleId: String,
    val email: String,
    val name: String,
    val picture: String,
    val googleToken: String
)
