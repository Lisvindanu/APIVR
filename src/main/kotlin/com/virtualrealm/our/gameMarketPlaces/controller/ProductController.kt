//heroku
package com.virtualrealm.our.gameMarketPlaces.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.virtualrealm.our.gameMarketPlaces.model.WebResponse
import com.virtualrealm.our.gameMarketPlaces.model.genre.GenreResponse
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.CreateProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ListProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ProductResponse
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.UpdateProductRequest
import com.virtualrealm.our.gameMarketPlaces.repository.GenreRepository
import com.virtualrealm.our.gameMarketPlaces.service.ProductService
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.nio.file.Paths


@RestController
@CrossOrigin
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
    private val objectMapper: ObjectMapper,
    @Value("\${file.upload.dir}") private val uploadDir: String
) {



    @PostMapping(
        produces = ["application/json"],
        consumes = ["multipart/form-data"]
    )
    fun createProduct(
        @RequestPart("body") body: String,
        @RequestPart(value = "file", required = false) file: MultipartFile?,
        @RequestHeader("X-Api-Key") apiKey: String
    ): WebResponse<ProductResponse> {
        try {
            val createProductRequest = objectMapper.readValue(body, CreateProductRequest::class.java)

            // Validate request first
            validateRequest(createProductRequest)

            // Then validate file if present
            file?.let {
                val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg", "image/svg+xml")
                if (!allowedTypes.contains(it.contentType)) {
                    throw IllegalArgumentException("Only image files (png, jpg, jpeg, svg) are allowed")
                }
            }

            val productResponse = productService.create(createProductRequest, file)

            return WebResponse(
                code = 200,
                status = "success",
                data = productResponse,
                message = "Product successfully created."
            )

        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message ?: "Invalid request")
        }
    }

    private fun validateRequest(request: CreateProductRequest) {
        val violations = mutableListOf<String>()

        if (request.name.isNullOrBlank()) violations.add("Name must not be blank")
        if (request.price == null || request.price < 1) violations.add("Price must be at least 1")
        if (request.quantity == null || request.quantity < 0) violations.add("Quantity must not be negative")
        if (request.categoryId == null) violations.add("Category ID must not be null")

        if (violations.isNotEmpty()) {
            throw IllegalArgumentException(violations.joinToString(", "))
        }
    }

    @PutMapping(value = ["/{id}"], consumes = ["multipart/form-data"])
    fun updateProduct(
        @PathVariable("id") id: Long,
        @RequestPart("body") body: String,
        @RequestPart(value = "file", required = false) file: MultipartFile?,
        @RequestHeader("X-Api-Key") apiKey: String
    ): WebResponse<ProductResponse> {
        try {
            val updateProductRequest = objectMapper.readValue(body, UpdateProductRequest::class.java)

            // Validate file if present
            file?.let {
                val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg", "image/svg+xml")
                if (!allowedTypes.contains(it.contentType)) {
                    throw IllegalArgumentException("Only image files (png, jpg, jpeg, svg) are allowed")
                }
            }

            val productResponse = productService.update(id, updateProductRequest, file)

            return WebResponse(
                code = 200,
                status = "success",
                data = productResponse,
                message = "Product successfully updated."
            )

        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message ?: "Invalid request")
        }
    }


    fun handleFileUpload(file: MultipartFile?, updateProductRequest: UpdateProductRequest): String {
        if (file != null) {
            val tika = Tika()
            val detectedType = tika.detect(file.inputStream)
            if (detectedType !in listOf("image/jpeg", "image/png")) {
                throw IllegalArgumentException("Unsupported file type: $detectedType")
            }

            val fileName = file.originalFilename ?: throw IllegalArgumentException("File name is required")
            val relativePath = "uploads/images/$fileName"  // Path relatif ke folder yang diatur di Spring
            val filePath = Paths.get(uploadDir, fileName).toString()  // `uploadDir` adalah path yang dikonfigurasi sebelumnya
            val imageFile = File(filePath)
            val directory = imageFile.parentFile
            if (!directory.exists()) {
                directory.mkdirs()
            }
            file.transferTo(imageFile) // Save the file to the local directory
            return relativePath // Return the relative path to the file
        }
        // Return default image URL if no file is uploaded
        return updateProductRequest.imageUrl ?: "/uploads/images/default-image.jpg"
    }


    @DeleteMapping(value = ["/{id}"], produces = ["application/json"])
    fun deleteProduct(
        @PathVariable("id") id: Long,
        @RequestHeader("X-Api-Key") apiKey: String
    ): WebResponse<Long> {
        productService.delete(id)
        return WebResponse(
            code = 200,
            status = "success",
            data = id,
            message = "Product successfully deleted."
        )

    }

    @GetMapping(produces = ["application/json"])
    fun listProducts(
        @RequestParam(value = "size", defaultValue = "10") size: Int,
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestHeader("X-Api-Key") apiKey: String
    ): WebResponse<List<ProductResponse>> {
        val request = ListProductRequest(page = page, size = size)
        val response = productService.list(request)
        return WebResponse(
            code = 200,
            status = "success",
            data = response,
            message = "Product list retrieved successfully."
        )

    }

    @GetMapping(value = ["/{id}"], produces = ["application/json"])
    fun getProductById(
        @PathVariable("id") id: Long,
        @RequestHeader("X-Api-Key") apiKey: String
    ): WebResponse<ProductResponse> {
        val productResponse = productService.getById(id)
        return WebResponse(
            code = 200,
            status = "success",
            data = productResponse,
            message = "Product details retrieved successfully."
        )

    }

    @GetMapping("/count", produces = ["application/json"])
    fun getProductCount(
        @RequestHeader("X-Api-Key") apiKey: String
    ): WebResponse<Long> {
        val count = productService.count()
        return WebResponse(
            code = 200,
            status = "success",
            data = count,
            message = "Total product count retrieved successfully."
        )
    }

}
