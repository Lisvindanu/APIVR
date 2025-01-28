package com.virtualrealm.our.gameMarketPlaces.model.Todo

import com.virtualrealm.our.gameMarketPlaces.entity.Priority
import java.time.LocalDateTime

data class TodoRequest(
    val title: String,
    val description: String? = null,
    val dueDate: LocalDateTime? = null,
    val priority: Priority = Priority.MEDIUM,
    val tags: Set<String> = setOf(),
    val remindAt: LocalDateTime? = null
)