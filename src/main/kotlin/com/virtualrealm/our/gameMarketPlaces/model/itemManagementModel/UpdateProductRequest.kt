package com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UpdateProductRequest(
    @field:NotBlank
    val name: String?,

    @field:NotNull
    @field:Min(1)
    val price: Long?,

    @field:NotNull
    @field:Min(0)
    val quantity: Int?,

    val categoryId: Long?, // Optional, allows updating the category

    val genreId: Long?, // Optional, allows updating the genre

    val imageUrl: String? // Optional, for updating the image URL
)
