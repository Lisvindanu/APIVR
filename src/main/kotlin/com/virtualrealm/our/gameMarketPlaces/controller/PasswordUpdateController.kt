package com.virtualrealm.our.gameMarketPlaces.controller

import com.virtualrealm.our.gameMarketPlaces.model.WebResponse
import com.virtualrealm.our.gameMarketPlaces.model.passwordUpdate.ChangePasswordRequest
import com.virtualrealm.our.gameMarketPlaces.service.impl.PasswordUpdateServiceImpl
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/users")
@CrossOrigin
class PasswordUpdateController(
    private val passwordUpdateService: PasswordUpdateServiceImpl
) {

    private val logger = LoggerFactory.getLogger(PasswordUpdateController::class.java)

    @PostMapping("/{userId}/change-password")
    fun changePassword(
        @PathVariable userId: Long,
        @RequestBody @Valid request: ChangePasswordRequest
    ): ResponseEntity<WebResponse<String>> {
        return processPasswordChange(userId, request)
    }

    @PostMapping("/auth/users/{userId}/change-password")
    fun changePasswordAuth(
        @PathVariable userId: Long,
        @RequestBody @Valid request: ChangePasswordRequest
    ): ResponseEntity<WebResponse<String>> {
        return processPasswordChange(userId, request)
    }

    private fun processPasswordChange(userId: Long, request: ChangePasswordRequest): ResponseEntity<WebResponse<String>> {
        return try {
            logger.info("Request to change password for user ID: $userId")
            passwordUpdateService.changePassword(userId, request)
            ResponseEntity.ok(WebResponse(
                code = 200,
                status = "success",
                data = "Password successfully updated",
                message = "Password has been changed successfully"
            ))
        } catch (e: IllegalArgumentException) {
            logger.error("Validation error: ${e.message}")
            ResponseEntity.badRequest().body(
                WebResponse(
                    code = 400,
                    status = "error",
                    data = null,
                    message = e.message ?: "Failed to change password"
                )
            )
        } catch (e: Exception) {
            logger.error("Unexpected error: ${e.message}")
            ResponseEntity.internalServerError().body(
                WebResponse(
                    code = 500,
                    status = "error",
                    data = null,
                    message = "An unexpected error occurred: ${e.message}"
                )
            )
        }
    }
}
