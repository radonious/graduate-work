package edu.plag.entity

import edu.plag.enums.UserRole
import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false)
    val password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole,

    val  refreshToken: String,

    @Column(name = "registered_at", nullable = false, updatable = false)
    val registeredAt: LocalDateTime = LocalDateTime.now()
)
