package com.virtualrealm.our.gameMarketPlaces.controller

import com.virtualrealm.our.gameMarketPlaces.model.WebResponse
import com.virtualrealm.our.gameMarketPlaces.model.passwordReset.ResetPasswordRequest
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import com.virtualrealm.our.gameMarketPlaces.service.impl.EmailService
import com.virtualrealm.our.gameMarketPlaces.service.impl.PasswordResetServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class PasswordResetController(
    private val passwordResetService: PasswordResetServiceImpl,
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {

    data class RequestResetPasswordBody(val email: String)

    @PostMapping("/request-reset")
    fun requestResetPassword(@RequestBody body: RequestResetPasswordBody): ResponseEntity<WebResponse<String>> {
        val email = body.email

        // Validate email format
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".toRegex()
        if (!email.matches(emailRegex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                WebResponse(400, "error", null, "Invalid email format")
            )
        }

        // Check if the user exists
        val userOptional = userRepository.findByEmail(email)
        if (!userOptional.isPresent) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                WebResponse(404, "error", null, "User not found")
            )
        }
        val user = userOptional.get()

        // Generate password reset token
        val resetToken = passwordResetService.generateResetToken(user.id!!)

        // Send password reset link to user's email
        emailService.sendPasswordResetLink(user.email, resetToken)

        // Return a successful response
        return ResponseEntity.ok(
            WebResponse(200, "success", "Reset link sent to ${user.email}", "Password reset email sent successfully")
        )
    }

    data class ResetPasswordBody(val token: String, val newPassword: String)

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody body: ResetPasswordBody): ResponseEntity<WebResponse<String>> {
        val token = body.token
        val newPassword = body.newPassword

        // Validate and reset the password
        passwordResetService.resetPassword(token, newPassword)
        return ResponseEntity.ok(
            WebResponse(200, "success", "Password reset successful", "Your password has been reset")
        )
    }
}
