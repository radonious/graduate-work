package edu.plag.dto

import java.time.LocalDate
import java.time.LocalTime

/**
 * Итоговый результат проверки.
 * Включает в себя: общую информацию, информацию о проверках для каждого файла
 */
data class CheckResults(
    val common: CommonCheckResults,
    val checks: List<SingleCheckResults>,
    val settings: CheckSettings,
)

/**
 * Результаты общие для всех видов проверок
 */
data class CommonCheckResults(
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val duration: Int,
    val checks: Int,
    val plagiarism: Double,
    val unique: Double = 1.0 - plagiarism,
)

/**
 * Общие результаты анализа для
 * 1 файла пользователя и N файлов из базы.
 * Содержит информацию о подозрительных файлах и результаты проверки с ними
 */
data class SingleCheckResults(
    val plagiarism: Double,
    val unique: Double = 1.0 - plagiarism,
    val source: FileInfo,
    val filesSuspectedByLexicalAnalyzer: List<LexicalPair>,
    val filesSuspectedBySyntaxAnalyzer: List<SyntaxPair>,
)

/**
 * Результаты лексического анализа для
 * 1 файла пользователя и N файлов из базы
 */
data class LexicalAnalyzerResults(
    val userCodeLexemesCount: Int,
    val dbCodeLexemesCount: Int,
    val longestCommonSubsequence: Int,
    val longestCommonSubsequenceScore: Double,
    val damerauLevenshteinDistance: Int,
    val damerauLevenshteinDistanceScore: Double,
    val jaccardSimilarityScore: Double,
    val kGramSimilarityScore: Double,
    val finalScore: Double,
)

data class LexicalPair(
    val file: FileInfo,
    val results: LexicalAnalyzerResults,
)

/**
 * Результаты синтаксического анализа для
 * 1 файла пользователя и N файлов из базы
 */
data class SyntaxAnalyzerResults(
    val hasIsomorphism: Boolean,
    val editDistance: Double,
    val editDistanceScore: Double,
    val finalScore: Double,
)

data class SyntaxPair(
    val file: FileInfo,
    val results: SyntaxAnalyzerResults,
)
