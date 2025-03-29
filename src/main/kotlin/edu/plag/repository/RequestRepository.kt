package edu.plag.repository

import edu.plag.entity.Request
import org.springframework.data.jpa.repository.JpaRepository

interface RequestRepository : JpaRepository<Request, Long> {
    fun findByUserId(userId: Long): List<Request>
}