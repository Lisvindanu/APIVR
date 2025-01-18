package com.virtualrealm.our.gameMarketPlaces.controller

import com.virtualrealm.our.gameMarketPlaces.entity.Todo
import com.virtualrealm.our.gameMarketPlaces.service.impl.TodoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoService) {

    @PostMapping
    fun addTodo(@RequestBody title: String): ResponseEntity<Todo> {
        val todo = todoService.addTodo(title)
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
        @RequestBody todo: Todo
    ): ResponseEntity<Todo> {
        val updatedTodo = todoService.updateTodo(id, todo.title, todo.isDone)
        return ResponseEntity.ok(updatedTodo)
    }

    @DeleteMapping("/{id}")
    fun deleteTodo(@PathVariable id: Long): ResponseEntity<Void> {
        todoService.deleteTodo(id)
        return ResponseEntity.noContent().build()
    }
}