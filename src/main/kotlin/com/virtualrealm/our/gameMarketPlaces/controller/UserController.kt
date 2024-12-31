package com.virtualrealm.our.gameMarketPlaces.controller

import com.virtualrealm.our.gameMarketPlaces.model.WebResponse
import com.virtualrealm.our.gameMarketPlaces.model.authModel.UpdateUserRequest
import com.virtualrealm.our.gameMarketPlaces.model.authModel.UserResponseData
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import com.virtualrealm.our.gameMarketPlaces.service.UserService
import com.virtualrealm.our.gameMarketPlaces.service.impl.AuthServicesImpl
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val authServicesImpl: AuthServicesImpl,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(UserController::class.java)


    @PutMapping("/update/{userId}")
    fun updateUserDetails(
        @PathVariable userId: Long,
        @RequestBody updateRequest: UpdateUserRequest
    ): ResponseEntity<WebResponse<UserResponseData>> {
        return try {
            // Call the service to update user details
            val updatedUser = authServicesImpl.updateUserDetails(userId, updateRequest)

            // Create and return the response
            val response = WebResponse(
                code = 200,
                status = "success",
                data = updatedUser,
                message = "User details updated successfully"
            )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error updating user details: ${e.message}")
            val response = WebResponse<UserResponseData>(
                code = 500,
                status = "error",
                data = null,
                message = "An unexpected error occurred: ${e.message}"
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
        }
    }

    @PutMapping("/profile/{userId}")
    fun updateProfile(
        @PathVariable userId: Long,
        @RequestBody updateRequest: UpdateUserRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<WebResponse<UserResponseData>> {
        return try {
            val token = authorization.removePrefix("Bearer ").trim()

            // Langsung gunakan updateRequest yang dikirim, karena sudah ada field fullname terpisah
            val updatedUser = authServicesImpl.updateProfile(userId, updateRequest, token)

            val response = WebResponse(
                code = 200,
                status = "success",
                data = updatedUser,
                message = "Profile updated successfully"
            )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error updating profile: ${e.message}")
            val response = WebResponse<UserResponseData>(
                code = 500,
                status = "error",
                data = null,
                message = "An unexpected error occurred: ${e.message}"
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
        }
    }

}
