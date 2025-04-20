package edu.plag.core.analyzer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.TimeUnit

@SpringBootTest
class LexicalAnalyzerTest(
    @Autowired private var lexicalAnalyzer: LexicalAnalyzer
) {
    private val emptyCode = ""
    private val codeA = "int a = 10;"
    private val identicalCode = codeA

    @Test
    fun `analyze should return zero score for empty inputs`() {
        val result = lexicalAnalyzer.analyze(emptyCode, emptyCode)

        assertEquals(0.0, result.finalScore)
        assertEquals(0, result.userCodeLexemesCount)
        assertEquals(0, result.dbCodeLexemesCount)
    }

    @Test
    fun `analyze should detect identical code`() {
        val result = lexicalAnalyzer.analyze(codeA, identicalCode)

        assertEquals(1.0, result.finalScore, 0.01)
        assertEquals(1.0, result.longestCommonSubsequenceScore, 0.01)
        assertEquals(1.0, result.damerauLevenshteinDistanceScore, 0.01)
    }

    @Test
    fun `LCS should return full length for identical tokens`() {
        val tokens = listOf("a", "b", "c")
        val result = lexicalAnalyzer.longestCommonSubsequence(tokens, tokens)

        assertEquals(3, result)
    }

    @Test
    fun `Damerau-Levenshtein should return zero for identical strings`() {
        val tokens = listOf("x", "y", "z")
        val result = lexicalAnalyzer.damerauLevenshteinDistance(tokens, tokens)

        assertEquals(0, result)
    }

    @Test
    fun `Jaccard score should be 1 for identical sets`() {
        val tokens = listOf("1", "2", "3")
        val score = lexicalAnalyzer.scoreJaccard(tokens, tokens)

        assertEquals(1.0, score, 0.01)
    }

    @Test
    fun `K-grams should handle small inputs`() {
        val tokens = listOf("a", "b")
        val score = lexicalAnalyzer.scoreKGrams(tokens, tokens, k = 3)

        assertEquals(0.0, score, 0.01) // Недостаточно токенов для 3-грамм
    }

    // Тесты производительности
    @RepeatedTest(20)
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    fun `performance test for large inputs`() {
        val bigCode = "val x = ${List(1000) { it.toString() }.joinToString(" ")}"

        val result = lexicalAnalyzer.analyze(bigCode, bigCode)

        assertTrue(result.finalScore >= 0.99)
    }
}