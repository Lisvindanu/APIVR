package com.virtualrealm.our.gameMarketPlaces.service

import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.CreateProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ListProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ProductResponse
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.UpdateProductRequest
import org.springframework.web.multipart.MultipartFile

interface ProductService {
    fun create(createProductRequest: CreateProductRequest, file: MultipartFile?): ProductResponse
    fun get(id: Long) : ProductResponse
    fun update(id: Long, updateProductRequest: UpdateProductRequest, file: MultipartFile?) : ProductResponse
    fun delete(id: Long)
    fun list(listProductRequest: ListProductRequest) : List<ProductResponse>
    fun getById(id: Long): ProductResponse
}