package com.virtualrealm.our.gameMarketPlaces.model.passwordUpdate

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)