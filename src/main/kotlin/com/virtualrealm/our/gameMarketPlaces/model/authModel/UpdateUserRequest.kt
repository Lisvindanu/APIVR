package com.virtualrealm.our.gameMarketPlaces.model.authModel

data class UpdateUserRequest(
    val username : String,
    val fullname : String,
    val password: String?,  // Optional
    val address: String?,   // Optional
    val phoneNumber: String? // Optional
)
