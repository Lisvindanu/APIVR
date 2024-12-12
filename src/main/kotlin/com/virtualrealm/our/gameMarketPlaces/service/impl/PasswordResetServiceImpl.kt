package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.entity.PasswordResetToken
import com.virtualrealm.our.gameMarketPlaces.entity.User
import com.virtualrealm.our.gameMarketPlaces.repository.PasswordResetTokenRepository
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import com.virtualrealm.our.gameMarketPlaces.service.PasswordResetService
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class PasswordResetServiceImpl(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService
) : PasswordResetService {

    // Generate reset token
    override fun generateResetToken(userId: Long): String {
        val token = (100000..999999).random().toString() // Generate a random token
        val expiresAt = LocalDateTime.now().plusMinutes(15) // Token expires in 15 minutes

        // Delete any existing token for the user
        passwordResetTokenRepository.deleteByUserId(userId)

        // Save the new token
        val resetToken = PasswordResetToken(
            userId = userId,
            token = token,
            expiresAt = expiresAt
        )
        passwordResetTokenRepository.save(resetToken)

        // Retrieve user email from the repository
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Send reset link to user's email with the generated token
        emailService.sendPasswordResetLink(user.email, token)

        return token
    }

    // Validate the reset token
    override fun validateResetToken(token: String): User {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow { IllegalArgumentException("Invalid or expired token") }

        if (resetToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("Token expired")
        }

        return userRepository.findById(resetToken.userId)
            .orElseThrow { IllegalArgumentException("User not found") }
    }

    // Reset password
    override fun resetPassword(token: String, newPassword: String) {
        val user = validateResetToken(token)
        val hashedPassword = hashPassword(newPassword) // Hash the new password

        val updatedUser = user.copy(password = hashedPassword)
        userRepository.save(updatedUser)

        // Delete the token after the password is reset
        passwordResetTokenRepository.deleteByUserId(user.id!!)
    }

    // Request reset password by email
    fun requestPasswordReset(email: String): String {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("User with email $email not found") }

        // Generate and send reset token
        return generateResetToken(user.id!!)
    }

    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt())
    }
}
