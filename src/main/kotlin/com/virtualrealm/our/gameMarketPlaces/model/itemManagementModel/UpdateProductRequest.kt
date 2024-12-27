package com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UpdateProductRequest(
    @field:NotBlank
    val name: String?,

    val description: String?,

    val specifications: String?,

    @field:NotNull
    @field:Min(1)
    val price: Long?,

    @field:NotNull
    @field:Min(0)
    val quantity: Int?,

    val categoryId: Long?,

    val genreIds: Set<Long>?,

    val imageUrl: String?
)
