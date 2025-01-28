package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.virtualrealm.our.gameMarketPlaces.entity.Priority
import com.virtualrealm.our.gameMarketPlaces.entity.Todo
import com.virtualrealm.our.gameMarketPlaces.model.Todo.TodoRequest
import com.virtualrealm.our.gameMarketPlaces.model.Todo.TodoStats
import com.virtualrealm.our.gameMarketPlaces.repository.PenggunaRepository
import com.virtualrealm.our.gameMarketPlaces.repository.TodoRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class TodoService(
    private val todoRepository: TodoRepository,
    private val penggunaRepository: PenggunaRepository,
    private val notificationService: NotificationService
) {
    fun addTodo(todoRequest: TodoRequest, email: String): Todo {
        val pengguna = penggunaRepository.findByEmail(email) ?: throw RuntimeException("Pengguna not found")

        val todo = Todo(
            title = todoRequest.title.trim(),
            description = todoRequest.description,
            dueDate = todoRequest.dueDate,
            priority = todoRequest.priority,
            tags = todoRequest.tags,
            remindAt = todoRequest.remindAt,
            owner = pengguna
        )

        val savedTodo = todoRepository.save(todo)

        // Schedule reminder if set
        todoRequest.remindAt?.let {
            notificationService.scheduleReminder(savedTodo)
        }

        return savedTodo
    }


    fun getTodoById(id: Long): Optional<Todo> {
        return todoRepository.findById(id)
    }

    fun getAllTodos(): List<Todo> {
        return todoRepository.findAll()
    }
    fun updateTodo(id: Long, todoRequest: TodoRequest): Todo {
        val todo = getTodoById(id).orElseThrow {
            RuntimeException("Todo not found with id: $id")
        }

        todo.apply {
            title = todoRequest.title.trim()
            description = todoRequest.description
            dueDate = todoRequest.dueDate
            priority = todoRequest.priority
            tags = todoRequest.tags
            remindAt = todoRequest.remindAt
        }

        return todoRepository.save(todo)
    }
    fun deleteTodo(id: Long) {
        val todo = todoRepository.findById(id).orElseThrow {
            RuntimeException("Todo not found with id: $id")
        }
        todoRepository.delete(todo)
    }

    fun searchTodos(
        query: String? = null,
        tags: Set<String>? = null,
        priority: Priority? = null,
        isDone: Boolean? = null,
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null
    ): List<Todo> {
        return todoRepository.search(query, tags, priority, isDone, fromDate, toDate)
    }

    fun shareTodo(todoId: Long, emails: List<String>): Todo {
        val todo = getTodoById(todoId).orElseThrow {
            RuntimeException("Todo not found with id: $todoId")
        }

        val pengguna = penggunaRepository.findAllByEmailIn(emails)
        todo.sharedWith = todo.sharedWith.plus(pengguna)

        return todoRepository.save(todo)
    }

    fun getTodoStats(penggunaId: Long): TodoStats {
        val todos = todoRepository.findAllByOwnerId(penggunaId)
        return TodoStats(
            totalTodos = todos.size,
            completedTodos = todos.count { it.isDone },
            pendingTodos = todos.count { !it.isDone },
            highPriorityTodos = todos.count { it.priority == Priority.HIGH },
            overdueTodos = todos.count {
                !it.isDone && it.dueDate?.isBefore(LocalDateTime.now()) ?: false
            },
            completionRate = if (todos.isNotEmpty()) {
                todos.count { it.isDone } * 100.0 / todos.size
            } else 0.0,
            averageCompletionTime = calculateAverageCompletionTime(todos)
        )
    }

    private fun calculateAverageCompletionTime(todos: List<Todo>): Duration? {
        val completedTodos = todos.filter { it.isDone }
        if (completedTodos.isEmpty()) return null

        val totalDuration = completedTodos.sumOf { todo ->
            Duration.between(todo.createdAt, todo.dueDate).toMillis()
        }
        return Duration.ofMillis(totalDuration / completedTodos.size)
    }
}