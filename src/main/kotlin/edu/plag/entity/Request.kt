package edu.plag.entity

import jakarta.persistence.*

@Entity
@Table(name = "requests")
data class Request(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = true)
    val file: File? = null,

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = true)
    val project: Project? = null
)
