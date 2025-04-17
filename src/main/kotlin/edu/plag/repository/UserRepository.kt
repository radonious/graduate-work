package edu.plag.repository

import edu.plag.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>

    fun findByRefreshToken(token: String): Optional<User>

    fun existsByUsername(username: String): Boolean
}
