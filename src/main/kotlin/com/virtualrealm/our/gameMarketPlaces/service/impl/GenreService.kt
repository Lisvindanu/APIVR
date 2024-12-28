package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.entity.Genre
import com.virtualrealm.our.gameMarketPlaces.repository.GenreRepository
import com.virtualrealm.our.gameMarketPlaces.repository.CategoryRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenreService(
    private val genreRepository: GenreRepository,
    private val categoryRepository: CategoryRepository
) {

    // Menambah genre baru
    fun addGenre(name: String, categoryId: Long): Genre {
        val category = categoryRepository.findById(categoryId).orElseThrow {
            RuntimeException("Category not found with id: $categoryId")
        }
        val genre = Genre(name = name, category = category)
        return genreRepository.save(genre)
    }

    // Mendapatkan genre berdasarkan ID
    fun getGenreById(id: Long): Optional<Genre> {
        return genreRepository.findById(id)
    }

    // Mendapatkan semua genre
    fun getAllGenres(): List<Genre> {
        return genreRepository.findAll()
    }

    // Mengubah genre berdasarkan ID
    fun updateGenre(id: Long, name: String, categoryId: Long): Genre {
        val genre = genreRepository.findById(id).orElseThrow {
            RuntimeException("Genre not found with id: $id")
        }
        val category = categoryRepository.findById(categoryId).orElseThrow {
            RuntimeException("Category not found with id: $categoryId")
        }
        genre.name = name
        genre.category = category
        return genreRepository.save(genre)
    }

    // Menghapus genre berdasarkan ID
    fun deleteGenre(id: Long) {
        val genre = genreRepository.findById(id).orElseThrow {
            RuntimeException("Genre not found with id: $id")
        }
        genreRepository.delete(genre)
    }

    fun count(): Long {
        return genreRepository.count()
    }
}
