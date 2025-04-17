package edu.plag.repository

import edu.plag.entity.Result
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResultsRepository : JpaRepository<Result, String> {

    fun findAllByUserId(userId: Long): List<Result>
}
