package com.virtualrealm.our.gameMarketPlaces.model.Todo

import com.virtualrealm.our.gameMarketPlaces.entity.Priority
import java.time.LocalDateTime

data class SearchRequest(
    val query: String? = null,
    val tags: Set<String>? = null,
    val priority: Priority? = null,
    val isDone: Boolean? = null,
    val fromDate: LocalDateTime? = null,
    val toDate: LocalDateTime? = null
)