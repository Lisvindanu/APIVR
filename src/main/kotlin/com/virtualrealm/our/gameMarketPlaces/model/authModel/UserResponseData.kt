package com.virtualrealm.our.gameMarketPlaces.model.authModel

data class UserResponseData(
    val id: Long,
    val username: String,
    val email: String,
    val address: String?,
    val phoneNumber: String?
)
