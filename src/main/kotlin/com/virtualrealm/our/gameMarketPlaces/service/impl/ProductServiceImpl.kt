package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.entity.Product
import com.virtualrealm.our.gameMarketPlaces.error.NotFoundException
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.CreateProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ListProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ProductResponse
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.UpdateProductRequest
import com.virtualrealm.our.gameMarketPlaces.repository.CategoryRepository
import com.virtualrealm.our.gameMarketPlaces.repository.GenreRepository
import com.virtualrealm.our.gameMarketPlaces.repository.ProductRepository
import com.virtualrealm.our.gameMarketPlaces.service.ProductService
import com.virtualrealm.our.gameMarketPlaces.validation.ValidationUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Paths
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
    val ftpService: FtpService,  // Add FtpService
    @Value("\${ftp.server}") val ftpServer: String,  // Add FTP configuration
    @Value("\${ftp.port}") val ftpPort: Int,
    @Value("\${ftp.username}") val ftpUsername: String,
    @Value("\${ftp.password}") val ftpPassword: String,
) : ProductService {

    override fun create(createProductRequest: CreateProductRequest, file: MultipartFile?): ProductResponse {
        validationUtil.validate(createProductRequest)

        val categoryId = createProductRequest.categoryId ?: throw IllegalArgumentException("Category ID must be provided")
        val category = categoryRepository.findByIdOrNull(categoryId)
            ?: throw NotFoundException("Category with ID $categoryId not found")

        val genre = createProductRequest.genreId?.let {
            val genreEntity = genreRepository.findByIdOrNull(it)
                ?: throw NotFoundException("Genre with ID $it not found")
            if (genreEntity.category.id != category.id) {
                throw IllegalArgumentException("Genre ID $it does not belong to the selected category")
            }
            genreEntity
        }

        // Handle file upload using FTP
        val imageUrl: String = file?.let {
            val fileName = it.originalFilename ?: throw IllegalArgumentException("File name is required")
            val remoteFilePath = "uploads/images/$fileName"

            // Upload to FTP server
            val uploadSuccess = ftpService.uploadFileToFtp(
                ftpServer,
                ftpPort,
                ftpUsername,
                ftpPassword,
                it,
                remoteFilePath
            )

            if (!uploadSuccess) {
                throw RuntimeException("Failed to upload file to FTP server")
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
            name = name,
            price = price,
            quantity = quantity,
            category = category,
            genre = genre,
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

    override fun update(id: Long, updateProductRequest: UpdateProductRequest): ProductResponse {
        val product = findProductByIdOrThrowNotFound(id)
        validationUtil.validate(updateProductRequest)

        // Fetch category and genre as in the create method
        val category = updateProductRequest.categoryId?.let {
            categoryRepository.findByIdOrNull(it) ?: throw NotFoundException("Category not found")
        } ?: product.category

        val genre = updateProductRequest.genreId?.let {
            val genreEntity = genreRepository.findByIdOrNull(it)
            if (genreEntity == null || genreEntity.category.id != category.id) {
                throw IllegalArgumentException("Invalid genre for the selected category")
            }
            genreEntity
        } ?: product.genre

        // Update the image URL if provided, otherwise retain the existing one
        val imageUrl = updateProductRequest.imageUrl ?: product.imageUrl  // This is the line you need to add

        product.apply {
            name = updateProductRequest.name!!
            price = updateProductRequest.price!!
            quantity = updateProductRequest.quantity!!
            this.category = category
            this.genre = genre
            this.imageUrl = imageUrl // Update the image URL
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
            price = product.price,
            quantity = product.quantity,
            categoryId = product.category.id,
            categoryName = product.category.name, // Get the category name
            genreId = product.genre?.id,
            genreName = product.genre?.name, // Get the genre name if available
            created_at = formatTimestamp(product.createdAt),
            updated_at = formatTimestamp(product.updatedAt),
            imageUrl = product.imageUrl // Include the image URL in the response
        )
    }


    private fun formatTimestamp(timestamp: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(timestamp) // Directly format the Date object
    }

    override fun getById(id: Long): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product with ID $id not found") }

        return ProductResponse(
            id = product.id,
            name = product.name,
            price = product.price,
            quantity = product.quantity,
            categoryId = product.category?.id,
            categoryName = product.category?.name,
            genreId = product.genre?.id,
            genreName = product.genre?.name,
            created_at = product.createdAt.toString(),
            updated_at = product.updatedAt.toString(),
            imageUrl = product.imageUrl
        )
    }

}
