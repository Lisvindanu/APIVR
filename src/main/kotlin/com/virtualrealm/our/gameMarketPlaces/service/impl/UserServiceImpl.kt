package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.entity.User
import com.virtualrealm.our.gameMarketPlaces.repository.OtpTokenRepository
import com.virtualrealm.our.gameMarketPlaces.service.UserService
import com.virtualrealm.our.gameMarketPlaces.service.impl.OtpService
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val otpService: OtpService,
    private val tokenRepository: OtpTokenRepository
) : UserService {

    private val logger = org.slf4j.LoggerFactory.getLogger(UserServiceImpl::class.java)

    // Fungsi untuk menandai OTP sebagai terverifikasi
    @Transactional
    override fun markOtpAsVerified(id: Long, inputOtp: String): User {
        // Retrieve the stored OTP for the user by their ID
        val storedOtp = otpService.getActiveOtpForUser(id)
            ?: throw IllegalArgumentException("No OTP found for user with ID: $id")

        // Validate if the input OTP matches the stored OTP
        if (storedOtp.otp != inputOtp) {
            throw IllegalArgumentException("Invalid OTP")
        }

        // If OTP is valid, mark it as verified and return the user
        val user = userRepository.findById(id).orElseThrow {
            IllegalArgumentException("User not found with ID: $id")
        }

        // You can update the user’s status or record in the database if necessary
        user.isOtpVerified = true
        userRepository.save(user)

        return user
    }

    override fun findByGoogleId(googleId: String): User? {
        return userRepository.findByGoogleId(googleId)
    }

    override fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    override fun getUsersByRole(role: String): List<User> {
        return userRepository.findByRole(role)
    }




}
