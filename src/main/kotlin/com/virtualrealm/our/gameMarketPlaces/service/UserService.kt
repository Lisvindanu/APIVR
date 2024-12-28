package com.virtualrealm.our.gameMarketPlaces.service

import com.virtualrealm.our.gameMarketPlaces.entity.User

interface UserService {
    fun findByGoogleId(googleId: String): User?
    fun markOtpAsVerified(id : Long, inputOtp: String):User
    fun getAllUsers(): List<User>
    fun getUsersByRole(role: String): List<User>
}