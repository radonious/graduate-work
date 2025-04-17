package edu.plag.entity

import edu.plag.enums.CheckType
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "results")
data class Result(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "timestamp", nullable = false)
    val timestamp: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "check_type", nullable = false)
    val checkType: CheckType,

    @Column(name = "plagiarism", nullable = false)
    val plagiarism: Double,

    @Column(name = "uniqueness", nullable = false)
    val uniqueness: Double,

    @Column(name = "lexical_enabled", nullable = false)
    val lexicalEnabled: Boolean,

    @Column(name = "syntax_enabled", nullable = false)
    val syntaxEnabled: Boolean
)
