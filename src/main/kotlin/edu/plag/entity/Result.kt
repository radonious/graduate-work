package edu.plag.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "results")
data class Result(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    val request: Request,

    @ManyToOne
    @JoinColumn(name = "most_similar_file_id", nullable = true)
    val mostSimilarFile: File? = null,

    @ManyToOne
    @JoinColumn(name = "most_similar_project_id", nullable = true)
    val mostSimilarProject: Project? = null,

    @Column(name = "final_score", nullable = false)
    val finalScore: Double,

    @Column(name = "analyzed_at", nullable = false, updatable = false)
    val analyzedAt: LocalDateTime = LocalDateTime.now()
)
