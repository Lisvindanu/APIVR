package com.virtualrealm.our.gameMarketPlaces.service

import com.virtualrealm.our.gameMarketPlaces.entity.User
import com.virtualrealm.our.gameMarketPlaces.entity.UserToken
import com.virtualrealm.our.gameMarketPlaces.model.authModel.*
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.multipart.MultipartFile

interface AuthServices {
    fun register(registerRequest: RegisterRequest): RegisterResponseData
    fun login(loginRequest: LoginRequest): LoginResponseData
    fun verifyOtpForRegistration(otpVerificationRequest: OtpVerificationRequest): RegisterResponseData
    fun logout(token: String)
    fun getUserData(token: String): UserDataResponse
    fun exchangeAuthCodeForToken(code: String): String
    fun registerOrLoginWithGoogle(userData: UserDataResponse): RegisterResponseData
    fun generateAndStoreToken(user: User): UserToken
    fun getUserDataFromGoogleToken(googleToken: String): UserDataResponse
    fun handleGoogleAuthentication(oauth2User: OAuth2User): User
    fun getOAuth2UserFromGoogleToken(googleToken: String): OAuth2User
    fun checkEmailExists(email: String): EmailCheckResponse
    fun updateProfile(
        userId: Long,
        updateRequest: UpdateUserRequest,
        token: String,
        file: MultipartFile? = null
    ): UserResponseData
}
