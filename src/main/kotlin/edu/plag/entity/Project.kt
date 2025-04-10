package edu.plag.entity

import jakarta.persistence.*

@Entity
@Table(name = "projects")
data class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
)