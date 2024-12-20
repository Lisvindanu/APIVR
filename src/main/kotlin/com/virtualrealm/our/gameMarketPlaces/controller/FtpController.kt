//package com.virtualrealm.our.gameMarketPlaces.controller
//
//import com.virtualrealm.our.gameMarketPlaces.service.impl.FtpService
//import org.springframework.web.bind.annotation.*
//import org.springframework.web.multipart.MultipartFile
//
//@RestController
//@RequestMapping("/ftp")
//class FtpController(private val ftpService: FtpService) {
//    companion object {
//        private const val CONNECT_TIMEOUT = 100000
//        private const val DATA_TIMEOUT = 150000
//    }
//
//    // Endpoint untuk menguji login FTP
//    @PostMapping("/test-login")
//    fun testFtpLogin(
//        @RequestParam server: String,
//        @RequestParam port: Int,
//        @RequestParam username: String,
//        @RequestParam password: String
//    ): Map<String, String> {
//        return if (ftpService.testLogin(server, port, username, password)) {
//            mapOf("status" to "success", "message" to "FTP login successful")
//        } else {
//            mapOf("status" to "failure", "message" to "FTP login failed")
//        }
//    }
//
//    // Endpoint untuk daftar file dalam direktori FTP
//    @GetMapping("/list-directory")
//    fun listFtpDirectory(
//        @RequestParam server: String,
//        @RequestParam port: Int,
//        @RequestParam username: String,
//        @RequestParam password: String,
//        @RequestParam directory: String
//    ): Map<String, Any> {
//        val files = ftpService.listFilesInDirectory(server, port, username, password, directory)
//        return if (files.isNotEmpty()) {
//            mapOf("status" to "success", "files" to files)
//        } else {
//            mapOf("status" to "failure", "message" to "Failed to list files or directory is empty")
//        }
//    }
//
//    // Endpoint untuk mengunggah file ke FTP
//    @PostMapping("/upload-file")
//    fun uploadFileToFtp(
//        @RequestParam server: String,
//        @RequestParam port: Int,
//        @RequestParam username: String,
//        @RequestParam password: String,
//        @RequestParam file: MultipartFile
//    ): Map<String, String> {
//        // Validasi tipe file
//        val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg", "image/svg+xml")
//        if (!allowedTypes.contains(file.contentType)) {
//            return mapOf("status" to "failure", "message" to "Only image files (png, jpg, jpeg, svg) are allowed")
//        }
//
//        // Tentukan path untuk upload ke direktori uploads/images/
//        val remoteFilePath = "uploads/images/${file.originalFilename}"
//
//        return if (ftpService.uploadFileToFtp(server, port, username, password, file, remoteFilePath)) {
//            mapOf("status" to "success", "message" to "File uploaded successfully to images directory")
//        } else {
//            mapOf("status" to "failure", "message" to "File upload failed")
//        }
//    }
//
//
//}

package com.virtualrealm.our.gameMarketPlaces.controller

import com.virtualrealm.our.gameMarketPlaces.service.impl.FtpService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/ftp")
class FtpController(private val ftpService: FtpService) {


    @PostMapping("/test-login")
    suspend fun testFtpLogin(
        @RequestParam server: String,
        @RequestParam port: Int,
        @RequestParam username: String,
        @RequestParam password: String
    ): Map<String, String> {
        return if (ftpService.testLogin(server, port, username, password)) {
            mapOf("status" to "success", "message" to "FTP login successful")
        } else {
            mapOf("status" to "failure", "message" to "FTP login failed")
        }
    }

    @GetMapping("/list-directory")
    suspend fun listFtpDirectory(
        @RequestParam server: String,
        @RequestParam port: Int,
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam directory: String
    ): Map<String, Any> {
        val files = ftpService.listFilesInDirectory(server, port, username, password, directory)
        return if (files.isNotEmpty()) {
            mapOf("status" to "success", "files" to files)
        } else {
            mapOf("status" to "failure", "message" to "Failed to list files or directory is empty")
        }
    }

    @PostMapping("/upload-file")
    suspend fun uploadFileToFtp(
        @RequestParam server: String,
        @RequestParam port: Int,
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam file: MultipartFile
    ): Map<String, String> {
        val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg", "image/svg+xml")
        if (!allowedTypes.contains(file.contentType)) {
            return mapOf("status" to "failure", "message" to "Only image files (png, jpg, jpeg, svg) are allowed")
        }
        val remoteFilePath = "uploads/images/${file.originalFilename}"
        return if (ftpService.uploadFileToFtp(server, port, username, password, file, remoteFilePath)) {
            mapOf("status" to "success", "message" to "File uploaded successfully to images directory")
        } else {
            mapOf("status" to "failure", "message" to "File upload failed")
        }
    }
}
