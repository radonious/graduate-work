package edu.plag.repository

import edu.plag.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>

    fun findByRefreshToken(token: String): Optional<User>

    fun existsByUsername(username: String): Boolean
}