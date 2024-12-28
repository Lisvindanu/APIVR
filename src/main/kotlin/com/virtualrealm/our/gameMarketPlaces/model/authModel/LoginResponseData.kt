package com.virtualrealm.our.gameMarketPlaces.model.authModel

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginResponseData(
    val token: String? = null,
    val expiresAt: LocalDateTime? = null,
    val message: String? = null,
    val status: String,
    val role: String? = null
)
