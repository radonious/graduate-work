package edu.plag.repository;

import edu.plag.entity.Result
import org.springframework.data.jpa.repository.JpaRepository

interface ResultRepository : JpaRepository<Result, Long> {
    fun findByRequestId(requestId: Long): List<Result>
    fun findByFinalScoreGreaterThan(score: Double): List<Result>
}