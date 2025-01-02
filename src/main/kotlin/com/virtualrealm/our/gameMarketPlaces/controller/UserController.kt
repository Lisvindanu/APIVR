package com.virtualrealm.our.gameMarketPlaces.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.virtualrealm.our.gameMarketPlaces.model.WebResponse
import com.virtualrealm.our.gameMarketPlaces.model.authModel.UpdateUserRequest
import com.virtualrealm.our.gameMarketPlaces.model.authModel.UserResponseData
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import com.virtualrealm.our.gameMarketPlaces.service.AuthServices
import com.virtualrealm.our.gameMarketPlaces.service.UserService
import com.virtualrealm.our.gameMarketPlaces.service.impl.AuthServicesImpl
import com.virtualrealm.our.gameMarketPlaces.service.impl.SftpService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@CrossOrigin
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val authServicesImpl: AuthServicesImpl,
    private val userRepository: UserRepository,
    private val sftpService: SftpService,
    private val objectMapper: ObjectMapper,
    private val authServices: AuthServices,
    @Value("\${sftp.server}") private val sftpServer: String,
    @Value("\${sftp.port}") private val sftpPort: Int,
    @Value("\${sftp.username}") private val sftpUsername: String,
    @Value("\${sftp.password}") private val sftpPassword: String
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

//    @PutMapping("/profile/{userId}")
//    fun updateProfile(
//        @PathVariable userId: Long,
//        @RequestBody updateRequest: UpdateUserRequest,
//        @RequestHeader("Authorization") authorization: String
//    ): ResponseEntity<WebResponse<UserResponseData>> {
//        return try {
//            val token = authorization.removePrefix("Bearer ").trim()
//
//            // Langsung gunakan updateRequest yang dikirim, karena sudah ada field fullname terpisah
//            val updatedUser = authServicesImpl.updateProfile(userId, updateRequest, token)
//
//            val response = WebResponse(
//                code = 200,
//                status = "success",
//                data = updatedUser,
//                message = "Profile updated successfully"
//            )
//            ResponseEntity.ok(response)
//        } catch (e: Exception) {
//            logger.error("Error updating profile: ${e.message}")
//            val response = WebResponse<UserResponseData>(
//                code = 500,
//                status = "error",
//                data = null,
//                message = "An unexpected error occurred: ${e.message}"
//            )
//            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
//        }
//    }

    @PutMapping("/profile/{userId}")
    fun updateProfile(
        @PathVariable userId: Long,
        @RequestPart("body") body: String,
        @RequestPart(value = "file", required = false) file: MultipartFile?,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<WebResponse<UserResponseData>> {
        return try {
            val token = authorization.removePrefix("Bearer ").trim()
            val updateRequest = objectMapper.readValue(body, UpdateUserRequest::class.java)

            // Handle file upload if exists
            val imageUrl = if (file != null) {
                val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
                val remoteFilePath = "/uploads/profiles/$fileName"

                logger.info("Starting file upload process for user $userId")

                val uploadSuccess = sftpService.uploadFileToSftp(
                    sftpServer,
                    sftpPort,
                    sftpUsername,
                    sftpPassword,
                    file,
                    remoteFilePath
                )

                if (!uploadSuccess) {
                    logger.error("Failed to upload profile picture for user $userId")
                    throw RuntimeException("Failed to upload profile picture")
                }

                logger.info("File upload successful for user $userId")
                "https://virtual-realm.my.id/uploads/profiles/$fileName"
            } else {
                updateRequest.imageUrl
            }

            // Update user profile dengan URL gambar baru jika ada
            val updatedUser = authServices.updateProfile(
                userId,
                updateRequest.copy(imageUrl = imageUrl),
                token,
                file  // Pass file to service
            )

            ResponseEntity.ok(WebResponse(
                code = 200,
                status = "success",
                data = updatedUser,
                message = "Profile updated successfully"
            ))

        } catch (e: Exception) {
            logger.error("Error updating profile: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(WebResponse(
                code = 500,
                status = "error",
                data = null,
                message = "An unexpected error occurred: ${e.message}"
            ))
        }
    }

}
