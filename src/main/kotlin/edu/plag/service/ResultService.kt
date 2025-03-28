package edu.plag.service

import edu.plag.entity.Result
import edu.plag.repository.ResultRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ResultService(private val resultRepository: ResultRepository) {

    fun getResultsByRequest(requestId: Long): List<Result> = resultRepository.findByRequestId(requestId)

    fun getResultsAboveThreshold(threshold: Double): List<Result> =
        resultRepository.findByFinalScoreGreaterThan(threshold)

    @Transactional
    fun saveResult(result: Result): Result = resultRepository.save(result)
}