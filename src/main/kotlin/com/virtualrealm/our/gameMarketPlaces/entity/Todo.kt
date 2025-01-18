package com.virtualrealm.our.gameMarketPlaces.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "todos")
data class Todo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var isDone:Boolean = false,


    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
