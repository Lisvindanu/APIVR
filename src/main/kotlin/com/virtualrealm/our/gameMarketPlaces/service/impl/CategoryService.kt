package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.entity.Category
import com.virtualrealm.our.gameMarketPlaces.repository.CategoryRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class CategoryService(private val categoryRepository: CategoryRepository) {

    // Menambah kategori baru
    fun addCategory(name: String): Category {
        val cleanedName = name.trim('"')
        val category = Category(name = cleanedName)
        return categoryRepository.save(category)
    }

    // Mendapatkan kategori berdasarkan ID
    fun getCategoryById(id: Long): Optional<Category> {
        return categoryRepository.findById(id)
    }

    // Mendapatkan semua kategori
    fun getAllCategories(): List<Category> {
        return categoryRepository.findAll()
    }

    // Mengubah kategori berdasarkan ID
    fun updateCategory(id: Long, name: String): Category {
        val category = categoryRepository.findById(id).orElseThrow {
            RuntimeException("Category not found with id: $id")
        }
        category.name = name.trim('"')
        return categoryRepository.save(category)
    }

    // Menghapus kategori berdasarkan ID
    fun deleteCategory(id: Long) {
        val category = categoryRepository.findById(id).orElseThrow {
            RuntimeException("Category not found with id: $id")
        }
        categoryRepository.delete(category)
    }

    fun count(): Long {
        return categoryRepository.count()
    }

}
