package com.virtualrealm.our.gameMarketPlaces.controller

import com.virtualrealm.our.gameMarketPlaces.model.WebResponse
import com.virtualrealm.our.gameMarketPlaces.model.passwordUpdate.ChangePasswordRequest
import com.virtualrealm.our.gameMarketPlaces.service.impl.PasswordUpdateServiceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@CrossOrigin
class PasswordUpdateController(
    private val passwordUpdateService: PasswordUpdateServiceImpl
) {

    @PostMapping("/{userId}/change-password")
    fun changePassword(
        @PathVariable userId: Long,
        @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<WebResponse<String>> {
        return try {
            passwordUpdateService.changePassword(userId, request)
            ResponseEntity.ok(WebResponse(
                code = 200,
                status = "success",
                data = "Password successfully updated",
                message = "Password has been changed successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                WebResponse(
                    code = 400,
                    status = "error",
                    data = null,
                    message = e.message ?: "Failed to change password"
                )
            )
        }
    }
}