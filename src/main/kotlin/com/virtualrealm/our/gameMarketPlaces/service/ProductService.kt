package com.virtualrealm.our.gameMarketPlaces.service

import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.CreateProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ListProductRequest
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.ProductResponse
import com.virtualrealm.our.gameMarketPlaces.model.itemManagementModel.UpdateProductRequest

interface ProductService {
    fun create(createProductRequest: CreateProductRequest) : ProductResponse
    fun get(id: String) : ProductResponse
    fun update(id: String, updateProductRequest: UpdateProductRequest) : ProductResponse
    fun delete(id: String)
    fun list(listProductRequest: ListProductRequest) : List<ProductResponse>
}