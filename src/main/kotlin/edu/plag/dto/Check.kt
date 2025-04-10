package edu.plag.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CheckSettings(
    @field:Min(1, message = "minFileLength should be 1 line or more")
    val minFileLength: Int,

    @field:DecimalMin("0.0", inclusive = false, message = "acceptableFileLengthDiff should be greater than 0.0")
    @field:DecimalMax("1.0", message = "acceptableFileLengthDiff should be  less than 1.0")
    val maxFileLengthDiffRate: Double,

    @field:DecimalMin("0.0", inclusive = false, message = "lexicalPlagiarismThreshold should be greater than 0.0")
    @field:DecimalMax("1.0", message = "lexicalPlagiarismThreshold should be less than 1.0")
    val lexicalPlagiarismThreshold: Double,
    val lexicalAnalysisEnable: Boolean,

    @field:DecimalMin("0.0", inclusive = false, message = "syntaxPlagiarismThreshold should be greater than 0.0")
    @field:DecimalMax("1.0", message = "syntaxAnalysisEnable should be less than 1.0")
    val syntaxPlagiarismThreshold: Double,
    val syntaxAnalysisEnable: Boolean,

    val saveSourcesIntoDatabase: Boolean,
)