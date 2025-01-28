package com.virtualrealm.our.gameMarketPlaces.repository

import com.virtualrealm.our.gameMarketPlaces.entity.Priority
import com.virtualrealm.our.gameMarketPlaces.entity.Todo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TodoRepository : JpaRepository<Todo, Long> {
    fun findAllByOwnerId(ownerId: Long): List<Todo>

    @Query("""
        SELECT t FROM Todo t 
        WHERE (:query IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:tags IS NULL OR t.tags IN :tags)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:isDone IS NULL OR t.isDone = :isDone)
        AND (:fromDate IS NULL OR t.dueDate >= :fromDate)
        AND (:toDate IS NULL OR t.dueDate <= :toDate)
    """)
    fun search(
        query: String?,
        tags: Set<String>?,
        priority: Priority?,
        isDone: Boolean?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?
    ): List<Todo>
}