//heroku

package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.entity.Product
import com.virtualrealm.our.gameMarketPlaces.error.NotFoundException
import com.virtualrealm.our.gameMarketPlaces.model.genre.GenreResponse
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.CreateProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ListProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ProductResponse
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.UpdateProductRequest
import com.virtualrealm.our.gameMarketPlaces.repository.CategoryRepository
import com.virtualrealm.our.gameMarketPlaces.repository.GenreRepository
import com.virtualrealm.our.gameMarketPlaces.repository.ProductRepository
import com.virtualrealm.our.gameMarketPlaces.service.ProductService
import com.virtualrealm.our.gameMarketPlaces.validation.ValidationUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors



@Service
class ProductServiceImpl(
    val productRepository: ProductRepository,
    val validationUtil: ValidationUtil,
    val categoryRepository: CategoryRepository,
    val genreRepository: GenreRepository,
    @Value("\${file.upload.dir}") val uploadDir: String,
    val sftpService: SftpService,  // Add FtpService
    @Value("\${sftp.server}") val sftpServer: String,  // Add sftp configuration
    @Value("\${sftp.port}") val sftpPort: Int,
    @Value("\${sftp.username}") val sftpUsername: String,
    @Value("\${sftp.password}") val sftpPassword: String,
) : ProductService {

    override fun create(createProductRequest: CreateProductRequest, file: MultipartFile?): ProductResponse {
        validationUtil.validate(createProductRequest)

        val categoryId = createProductRequest.categoryId ?: throw IllegalArgumentException("Category ID must be provided")
        val category = categoryRepository.findByIdOrNull(categoryId)
            ?: throw NotFoundException("Category with ID $categoryId not found")

        val genres = createProductRequest.genreIds?.map { genreId ->
            val genre = genreRepository.findByIdOrNull(genreId)
                ?: throw NotFoundException("Genre with ID $genreId not found")
            if (genre.category.id != category.id) {
                throw IllegalArgumentException("Genre ID $genreId does not belong to the selected category")
            }
            genre
        }?.toMutableSet() ?: mutableSetOf()

        // Handle file upload using sftp
        val imageUrl: String = file?.let {
            val fileName = it.originalFilename ?: throw IllegalArgumentException("File name is required")
            val remoteFilePath = "/uploads/images/$fileName"

            // Upload to sftp server
            val uploadSuccess = sftpService.uploadFileToSftp(
                sftpServer,
                sftpPort,
                sftpUsername,
                sftpPassword,
                it,
                remoteFilePath
            )

            if (!uploadSuccess) {
                throw RuntimeException("Failed to upload file to sftp server")
            }

            "/uploads/images/$fileName"  // Return the URL path
        } ?: "null"

        val name = createProductRequest.name
        val price = createProductRequest.price
        val quantity = createProductRequest.quantity

        if (name.isNullOrBlank() || price == null || quantity == null) {
            throw IllegalArgumentException("Product name, price, and quantity must be provided")
        }

        val product = Product(
            id = createProductRequest.id,
            name = createProductRequest.name ?: throw IllegalArgumentException("Name must be provided"),
            description = createProductRequest.description,
            specifications = createProductRequest.specifications,
            price = createProductRequest.price ?: throw IllegalArgumentException("Price must be provided"),
            quantity = createProductRequest.quantity ?: throw IllegalArgumentException("Quantity must be provided"),
            category = category,
            genres = genres,
            createdAt = Date(),
            updatedAt = Date(),
            imageUrl = imageUrl
        )

        productRepository.save(product)
        return convertProductToProductResponse(product)
    }


    override fun get(id: Long): ProductResponse {
        val product = findProductByIdOrThrowNotFound(id)
        return convertProductToProductResponse(product)
    }

    override fun update(id: Long, updateProductRequest: UpdateProductRequest, file: MultipartFile?): ProductResponse {
        val product = findProductByIdOrThrowNotFound(id)
        validationUtil.validate(updateProductRequest)

        // Fetch category and genre
        val category = updateProductRequest.categoryId?.let {
            categoryRepository.findByIdOrNull(it) ?: throw NotFoundException("Category not found")
        } ?: product.category

        val genres = updateProductRequest.genreIds?.map { genreId ->
            val genre = genreRepository.findByIdOrNull(genreId)
                ?: throw NotFoundException("Genre with ID $genreId not found")
            if (genre.category.id != category.id) {
                throw IllegalArgumentException("Genre ID $genreId does not belong to the selected category")
            }
            genre
        }?.toMutableSet() ?: product.genres

        // Handle file upload using sftp if a new file is provided
        val imageUrl = file?.let {
            val fileName = it.originalFilename ?: throw IllegalArgumentException("File name is required")
            val remoteFilePath = "/uploads/images/$fileName"

            // Upload to sftp server
            val uploadSuccess = sftpService.uploadFileToSftp(
                sftpServer,
                sftpPort,
                sftpUsername,
                sftpPassword,
                it,
                remoteFilePath
            )

            if (!uploadSuccess) {
                throw RuntimeException("Failed to upload file to sftp server")
            }

            "/uploads/images/$fileName"  // Return the URL path
        } ?: product.imageUrl // Keep existing image URL if no new file is uploaded

        product.apply {
            name = updateProductRequest.name!!
            description = updateProductRequest.description
            specifications = updateProductRequest.specifications
            price = updateProductRequest.price!!
            quantity = updateProductRequest.quantity!!
            this.category = category
            this.genres = genres
            this.imageUrl = imageUrl
            updatedAt = Date()
        }

        productRepository.save(product)
        return convertProductToProductResponse(product)
    }


    override fun delete(id: Long) {
        val product = findProductByIdOrThrowNotFound(id)
        productRepository.delete(product)
    }

    override fun list(listProductRequest: ListProductRequest): List<ProductResponse> {
        val page = productRepository.findAll(PageRequest.of(listProductRequest.page, listProductRequest.size))
        val products: List<Product> = page.get().collect(Collectors.toList())
        return products.map { convertProductToProductResponse(it) }
    }

    private fun findProductByIdOrThrowNotFound(id: Long): Product {
        return productRepository.findByIdOrNull(id) ?: throw NotFoundException("Product not found")
    }

    private fun convertProductToProductResponse(product: Product): ProductResponse {
        return ProductResponse(
            id = product.id,
            name = product.name,
            description = product.description,
            specifications = product.specifications,
            price = product.price,
            quantity = product.quantity,
            categoryId = product.category.id,
            categoryName = product.category.name,
            genres = product.genres.map { genre ->
                GenreResponse(
                    id = genre.id!!,
                    name = genre.name,
                    categoryId = genre.category.id!!
                )
            },
            created_at = formatTimestamp(product.createdAt),
            updated_at = formatTimestamp(product.updatedAt),
            imageUrl = product.imageUrl
        )
    }


    private fun formatTimestamp(timestamp: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(timestamp) // Directly format the Date object
    }

    override fun getById(id: Long): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product with ID $id not found") }

        return convertProductToProductResponse(product)
    }


}
