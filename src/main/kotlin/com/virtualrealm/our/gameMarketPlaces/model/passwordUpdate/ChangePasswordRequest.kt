package com.virtualrealm.our.gameMarketPlaces.model.passwordUpdate

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password must not be blank")
    val currentPassword: String,

    @field:NotBlank(message = "New password must not be blank")
    @field:Size(min = 8, message = "New password must be at least 8 characters long")
    val newPassword: String,

    @field:NotBlank(message = "Confirm password must not be blank")
    val confirmPassword: String
)