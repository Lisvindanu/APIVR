package com.virtualrealm.our.gameMarketPlaces.model.authModel

data class EmailCheckResponse(
    val exists: Boolean,
    val uuid: String,
    val fullname: String?,
    val username: String,
    val email: String,
    val image: String?,
    val isLoggedIn: Boolean
)