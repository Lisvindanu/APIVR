package com.virtualrealm.our.gameMarketPlaces.controller

import com.virtualrealm.our.gameMarketPlaces.entity.Todo
import com.virtualrealm.our.gameMarketPlaces.model.Todo.SearchRequest
import com.virtualrealm.our.gameMarketPlaces.model.Todo.TodoRequest
import com.virtualrealm.our.gameMarketPlaces.model.Todo.TodoStats
import com.virtualrealm.our.gameMarketPlaces.repository.PenggunaRepository
import com.virtualrealm.our.gameMarketPlaces.service.impl.TodoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoService, private val penggunaRepository: PenggunaRepository) {

    @PostMapping
    fun addTodo(
        @RequestBody todoRequest: TodoRequest,
        @RequestHeader("X-Email") email: String
    ): ResponseEntity<Todo> {
        val todo = todoService.addTodo(todoRequest, email)
        return ResponseEntity.ok(todo)
    }


    @GetMapping("/{id}")
    fun getTodoById(@PathVariable id: Long): ResponseEntity<Todo> {
        val todo = todoService.getTodoById(id).orElseThrow {
            RuntimeException("Todo not found with id: $id")
        }
        return ResponseEntity.ok(todo)
    }

    @GetMapping
    fun getAllTodos(): ResponseEntity<List<Todo>> {
        val todos = todoService.getAllTodos()
        return ResponseEntity.ok(todos)
    }

    @PutMapping("/{id}")
    fun updateTodo(
        @PathVariable id: Long,
        @RequestBody todoRequest: TodoRequest
    ): ResponseEntity<Todo> {
        val todo = todoService.updateTodo(id, todoRequest)
        return ResponseEntity.ok(todo)
    }

    @DeleteMapping("/{id}")
    fun deleteTodo(@PathVariable id: Long): ResponseEntity<Void> {
        todoService.deleteTodo(id)
        return ResponseEntity.noContent().build()
    }


    @PostMapping("/{id}/share")
    fun shareTodo(
        @PathVariable id: Long,
        @RequestBody emails: List<String>
    ): ResponseEntity<Todo> {
        val todo = todoService.shareTodo(id, emails)
        return ResponseEntity.ok(todo)
    }

    @PostMapping("/search")
    fun searchTodos(
        @RequestBody searchRequest: SearchRequest
    ): ResponseEntity<List<Todo>> {
        val todos = todoService.searchTodos(
            searchRequest.query,
            searchRequest.tags,
            searchRequest.priority,
            searchRequest.isDone,
            searchRequest.fromDate,
            searchRequest.toDate
        )
        return ResponseEntity.ok(todos)
    }

    @GetMapping("/stats")
    fun getTodoStats(
        @RequestHeader("X-Email") email: String
    ): ResponseEntity<TodoStats> {
        val pengguna = penggunaRepository.findByEmail(email)
            ?: throw RuntimeException("Pengguna not found")
        val stats = todoService.getTodoStats(pengguna.id!!)
        return ResponseEntity.ok(stats)
    }
}