package com.virtualrealm.our.gameMarketPlaces.model.authModel

data class UserDataResponse(
    val username: String,
    val fullname: String?,
    val email: String,
    val googleId: String?,
    val imageUrl: String?,
    val uuid: String
)