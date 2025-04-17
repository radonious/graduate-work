package edu.plag.service

import edu.plag.dto.CheckResults
import edu.plag.entity.Result
import edu.plag.enums.CheckType
import edu.plag.repository.ResultsRepository
import org.springframework.stereotype.Service

@Service
class ResultService(
    private val resultsRepository: ResultsRepository
) {

    fun getResults(userId: Long): List<Result> {
        return resultsRepository.findAllByUserId(userId)
    }

    fun saveResults(userId: Long, results: CheckResults, type: CheckType) {
        val resultsEntity = Result(
            userId = userId,
            checkType = type,
            plagiarism = results.common.plagiarism,
            uniqueness = results.common.unique,
            lexicalEnabled = results.settings.lexicalAnalysisEnable,
            syntaxEnabled = results.settings.syntaxAnalysisEnable,
        )

        resultsRepository.save(resultsEntity)
    }
}
