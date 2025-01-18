package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.entity.Todo
import com.virtualrealm.our.gameMarketPlaces.repository.TodoRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class TodoService(private val todoRepository: TodoRepository) {
    fun addTodo(title: String): Todo {
        val todo = Todo(title = title.trim())
        return todoRepository.save(todo)
    }

    fun getTodoById(id: Long): Optional<Todo> {
        return todoRepository.findById(id)
    }

    fun getAllTodos(): List<Todo> {
        return todoRepository.findAll()
    }

    fun updateTodo(id: Long, title: String, isDone: Boolean): Todo {
        val todo = todoRepository.findById(id).orElseThrow {
            RuntimeException("Todo not found with id: $id")
        }
        todo.title = title.trim()
        todo.isDone = isDone
        return todoRepository.save(todo)
    }

    fun deleteTodo(id: Long) {
        val todo = todoRepository.findById(id).orElseThrow {
            RuntimeException("Todo not found with id: $id")
        }
        todoRepository.delete(todo)
    }
}