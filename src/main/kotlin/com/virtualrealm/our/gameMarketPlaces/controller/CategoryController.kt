package com.virtualrealm.our.gameMarketPlaces.controller

import com.virtualrealm.our.gameMarketPlaces.service.impl.CategoryService
import com.virtualrealm.our.gameMarketPlaces.entity.Category
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(private val categoryService: CategoryService) {

    // Menambahkan kategori baru
    @PostMapping
    fun addCategory(@RequestBody name: String): Category {
        return categoryService.addCategory(name)
    }

    // Mendapatkan kategori berdasarkan ID
    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): ResponseEntity<Category> {
        val category = categoryService.getCategoryById(id).orElseThrow {
            RuntimeException("Category not found with id: $id")
        }
        return ResponseEntity.ok(category)
    }

    // Mendapatkan semua kategori
    @GetMapping
    fun getAllCategories(): ResponseEntity<List<Category>> {
        val categories = categoryService.getAllCategories()
        return ResponseEntity.ok(categories)
    }

    // Mengubah kategori berdasarkan ID
    @PutMapping("/{id}")
    fun updateCategory(@PathVariable id: Long, @RequestBody name: String): Category {
        return categoryService.updateCategory(id, name)
    }

    // Menghapus kategori berdasarkan ID
    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Void> {
        categoryService.deleteCategory(id)
        return ResponseEntity.noContent().build()
    }
}
