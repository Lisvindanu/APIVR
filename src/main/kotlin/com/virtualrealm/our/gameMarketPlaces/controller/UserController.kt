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
import java.io.File
import java.nio.file.Paths
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
    @Value("\${sftp.password}") private val sftpPassword: String,
    @Value("\${file.upload.dir}") val uploadDir: String,

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
        @RequestPart("body") body: String,
        @RequestPart(value = "file", required = false) file: MultipartFile?,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<WebResponse<UserResponseData>> {
        return try {
            val token = authorization.removePrefix("Bearer ").trim()
            val updateRequest = objectMapper.readValue(body, UpdateUserRequest::class.java)

            // Handle file upload if present
            val imageUrl = handleFileUpload(file, updateRequest)

            // Update user profile
            val updatedUser = authServices.updateProfile(
                userId,
                updateRequest.copy(imageUrl = imageUrl),
                token
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

    private fun handleFileUpload(file: MultipartFile?, updateRequest: UpdateUserRequest): String {
        if (file != null) {
            try {
                val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg", "image/svg+xml")
                val contentType = file.contentType ?: throw IllegalArgumentException("File type is required")

                if (!allowedTypes.contains(contentType)) {
                    throw IllegalArgumentException("Unsupported file type: $contentType")
                }

                val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
                val relativePath = "uploads/images/$fileName"
                val filePath = Paths.get(uploadDir, fileName).toString()
                val imageFile = File(filePath)

                // Create directories if not exist
                imageFile.parentFile.mkdirs()

                // Save file
                file.transferTo(imageFile)
                logger.info("File saved successfully: $filePath")

                return "https://virtual-realm.my.id/$relativePath"
            } catch (e: Exception) {
                logger.error("Error saving file: ${e.message}")
                throw RuntimeException("File upload failed: ${e.message}")
            }
        }

        // Return existing imageUrl if file is not provided
        return updateRequest.imageUrl ?: "/uploads/images/default-profile.jpg"
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
