package edu.plag.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CheckSettings(
    @field:Min(1, message = "minFileSize should be 1 or more")
    val minFileSize: Int,
    @field:DecimalMin("0.0", inclusive = false, message = "lexicalPlagiarismThreshold greater than 0.0")
    @field:DecimalMax("1.0", message = "lexicalPlagiarismThreshold less than 1.0")
    val lexicalPlagiarismThreshold: Double,
    val lexicalAnalysisEnable: Boolean,
    @field:DecimalMin("0.0", inclusive = false, message = "syntaxPlagiarismThreshold greater than 0.0")
    @field:DecimalMax("1.0", message = "syntaxAnalysisEnable less than 1.0")
    val syntaxPlagiarismThreshold: Double,
    val syntaxAnalysisEnable: Boolean,
    val checkComments: Boolean,
    @field:NotNull(message = "Preset is mandatory. Presets: SPEED, NORMAL, QUALITY")
    val preset: Preset,
    val saveFileInDatabase: Boolean,
)

enum class Preset {
    SPEED, NORMAL, QUALITY
}