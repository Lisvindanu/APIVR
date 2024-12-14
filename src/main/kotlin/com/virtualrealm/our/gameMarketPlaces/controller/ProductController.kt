package com.virtualrealm.our.gameMarketPlaces.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.virtualrealm.our.gameMarketPlaces.model.WebResponse
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.CreateProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ListProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ProductResponse
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.UpdateProductRequest
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
        consumes = ["multipart/form-data", "application/json"],
        produces = ["application/json"]
    )
    fun createProduct(
        @RequestPart(value = "body", required = false) body: String?,
        @RequestPart(value = "file", required = false) file: MultipartFile?,
        @RequestHeader("X-Api-Key") apiKey: String
    ): WebResponse<ProductResponse> {
        // If the body part is provided, convert it to CreateProductRequest
        val createProductRequest = body?.let {
            objectMapper.readValue(it, CreateProductRequest::class.java)
        } ?: throw IllegalArgumentException("Missing request body (either JSON or multipart)")

        // Validate the request
        validateRequest(createProductRequest)

        // Validate the file if present
        file?.let {
            val tika = Tika()
            val detectedType = tika.detect(it.inputStream)
            if (detectedType !in listOf("image/jpeg", "image/png")) {
                throw IllegalArgumentException("Unsupported file type: $detectedType")
            }
        }

        // Process product creation
        val productResponse = productService.create(createProductRequest, file)

        return WebResponse(
            code = 200,
            status = "success",
            data = productResponse
        )
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
        @RequestPart("body") body: String?,
        @RequestPart(value = "file", required = false) file: MultipartFile?,
        @RequestHeader("X-Api-Key") apiKey: String
    ): WebResponse<ProductResponse> {
        return try {
            val updateProductRequest = body?.let {
                objectMapper.readValue(it, UpdateProductRequest::class.java)
            } ?: throw IllegalArgumentException("Missing request body (either JSON or multipart)")

            // Handle file upload logic if a file is provided
            val imageUrl = handleFileUpload(file, updateProductRequest)

            // Update the product with the file information (if any)
            val productResponse = productService.update(id, updateProductRequest.copy(imageUrl = imageUrl))

            WebResponse(
                code = 200,
                status = "success",
                data = productResponse
            )
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message ?: "Invalid request")
        } catch (e: Exception) {
            e.printStackTrace() // Log the stack trace
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the product: ${e.message}")
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
            val relativePath = "/uploads/images/$fileName"  // Define the image path
            val filePath = Paths.get(uploadDir, fileName).toString()
            val imageFile = File(filePath)
            val directory = imageFile.parentFile
            if (!directory.exists()) {
                directory.mkdirs()
            }
            file.transferTo(imageFile) // Save file
            return relativePath
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
            data = id
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
            data = response
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
            data = productResponse
        )
    }

}
