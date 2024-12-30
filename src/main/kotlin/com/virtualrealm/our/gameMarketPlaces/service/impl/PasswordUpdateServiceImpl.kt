package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.model.passwordUpdate.ChangePasswordRequest
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PasswordUpdateServiceImpl(
    private val userRepository: UserRepository
) {

    @Transactional
    fun changePassword(userId: Long, request: ChangePasswordRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Verify current password
        if (!BCrypt.checkpw(request.currentPassword, user.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        // Verify password confirmation
        if (request.newPassword != request.confirmPassword) {
            throw IllegalArgumentException("New password and confirmation do not match")
        }

        // Update password
        val hashedNewPassword = BCrypt.hashpw(request.newPassword, BCrypt.gensalt())
        user.password = hashedNewPassword
        userRepository.save(user)
    }
}