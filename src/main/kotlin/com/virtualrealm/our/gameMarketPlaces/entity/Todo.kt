package com.virtualrealm.our.gameMarketPlaces.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "todos")
data class Todo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "due_date")
    var dueDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    var priority: Priority = Priority.MEDIUM,

    @ElementCollection
    @CollectionTable(name = "todo_tags", joinColumns = [JoinColumn(name = "todo_id")])
    @Column(name = "tag")
    var tags: Set<String> = setOf(),

    @Column(nullable = false)
    var isDone: Boolean = false,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "remind_at")
    var remindAt: LocalDateTime? = null,

    @ManyToMany
    @JoinTable(
        name = "todo_shares",
        joinColumns = [JoinColumn(name = "todo_id")],
        inverseJoinColumns = [JoinColumn(name = "pengguna_id")]
    )
    var sharedWith: Set<Pengguna> = setOf(),

    @ManyToOne
    @JoinColumn(name = "pengguna_id")
    var owner: Pengguna? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)