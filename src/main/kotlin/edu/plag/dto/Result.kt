package edu.plag.dto

import java.time.LocalDate
import java.time.LocalTime

// TODO: (PRIORITY) продумать как считать результаты для отдельных файлов и проекта
//data class ProjectCheckResults(
//    val date: LocalDate = LocalDate.now(),
//    val time: LocalTime = LocalTime.now(),
//    val filesSuspectedByLexicalAnalyzer: Map<FileInfo, LexicalAnalyzerResults>,
//    val filesSuspectedBySyntaxAnalyzer: Map<FileInfo, SyntaxAnalyzerResults>,
//)

data class CheckResults(
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val filesSuspectedByLexicalAnalyzer: Map<FileInfo, LexicalAnalyzerResults>,
    val filesSuspectedBySyntaxAnalyzer: Map<FileInfo, SyntaxAnalyzerResults>,
)

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

data class SyntaxAnalyzerResults(
    val hasIsomorphism: Boolean,
    val editDistance: Double,
    val editDistanceScore: Double,
)