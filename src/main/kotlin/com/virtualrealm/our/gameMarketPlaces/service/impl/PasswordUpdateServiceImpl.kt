package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.model.passwordUpdate.ChangePasswordRequest
import com.virtualrealm.our.gameMarketPlaces.repository.TokenRepository
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory

@Service
class PasswordUpdateServiceImpl(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository
) {

    private val logger = LoggerFactory.getLogger(PasswordUpdateServiceImpl::class.java)

    @Transactional
    fun changePassword(userId: Long, request: ChangePasswordRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        logger.info("Validating current password for user ID: $userId")

        // Validasi password lama
        if (!BCrypt.checkpw(request.currentPassword, user.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        // Validasi password baru dan konfirmasi
        if (request.newPassword != request.confirmPassword) {
            throw IllegalArgumentException("New password and confirmation do not match")
        }

        if (request.newPassword.length < 8) {
            throw IllegalArgumentException("New password must be at least 8 characters long")
        }

        logger.info("Updating password for user ID: $userId")

        // Hash password baru
        val hashedPassword = BCrypt.hashpw(request.newPassword, BCrypt.gensalt())
        user.password = hashedPassword

        // Simpan user dengan password baru
        userRepository.save(user)

        // Hapus semua token aktif
        logger.info("Deleting all active tokens for user ID: $userId")
        tokenRepository.deleteAllByUserId(userId)

        logger.info("Password successfully updated for user ID: $userId")
    }
}
