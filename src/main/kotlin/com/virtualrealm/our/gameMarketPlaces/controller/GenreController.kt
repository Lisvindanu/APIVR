package com.virtualrealm.our.gameMarketPlaces.controller

import com.virtualrealm.our.gameMarketPlaces.service.impl.GenreService
import com.virtualrealm.our.gameMarketPlaces.entity.Genre
import com.virtualrealm.our.gameMarketPlaces.model.WebResponse
import com.virtualrealm.our.gameMarketPlaces.repository.GenreRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/genres")
class GenreController(
    private val genreService: GenreService,
    private val genreRepository: GenreRepository
) {

    // Menambah genre baru
    @PostMapping
    fun addGenre(@RequestParam name: String, @RequestParam categoryId: Long): Genre {
        return genreService.addGenre(name, categoryId)
    }

    // Mendapatkan genre berdasarkan ID
    @GetMapping("/{id}")
    fun getGenreById(@PathVariable id: Long): ResponseEntity<Genre> {
        val genre = genreService.getGenreById(id).orElseThrow {
            RuntimeException("Genre not found with id: $id")
        }
        return ResponseEntity.ok(genre)
    }

    // Mendapatkan semua genre
    @GetMapping
    fun getAllGenres(): ResponseEntity<List<Genre>> {
        val genres = genreService.getAllGenres()
        return ResponseEntity.ok(genres)
    }

    // Mengubah genre berdasarkan ID
    @PutMapping("/{id}")
    fun updateGenre(@PathVariable id: Long, @RequestParam name: String, @RequestParam categoryId: Long): Genre {
        return genreService.updateGenre(id, name, categoryId)
    }

    // Menghapus genre berdasarkan ID
    @DeleteMapping("/{id}")
    fun deleteGenre(@PathVariable id: Long): ResponseEntity<Void> {
        genreService.deleteGenre(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/count", produces = ["application/json"])
    fun getGenreCount(@RequestHeader("X-Api-Key") apiKey: String): WebResponse<Long> {
        val count = genreService.count()
        return WebResponse(
            code = 200,
            status = "success",
            data = count,
            message = "Total genre count retrieved successfully."
        )
    }
}
