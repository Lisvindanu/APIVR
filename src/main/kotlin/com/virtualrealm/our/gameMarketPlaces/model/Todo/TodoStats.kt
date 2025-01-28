package com.virtualrealm.our.gameMarketPlaces.model.Todo

import java.time.Duration


data class TodoStats(
    val totalTodos: Int,
    val completedTodos: Int,
    val pendingTodos: Int,
    val highPriorityTodos: Int,
    val overdueTodos: Int,
    val completionRate: Double,
    val averageCompletionTime: Duration?
)