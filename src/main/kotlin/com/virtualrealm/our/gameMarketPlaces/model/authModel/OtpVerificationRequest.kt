package com.virtualrealm.our.gameMarketPlaces.model.authModel

data class OtpVerificationRequest(
    val email:String,
    val otp:String
)
