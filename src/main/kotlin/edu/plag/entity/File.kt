package edu.plag.entity

import jakarta.persistence.*

@Entity
@Table(name = "files")
data class File(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = true)
    val project: Project? = null,

    @Column(nullable = false)
    val filename: String,

    @Column(nullable = false, unique = true)
    val path: String,

    @Column(name = "line_count")
    val lineCount: Int = 0,
)