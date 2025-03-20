package edu.plag.analysis.analyzer

import edu.plag.analysis.parser.TokenParser
import edu.plag.util.CodeSnippets
import kotlin.math.*

class TokenAnalyzer {

    /**
     * Весовые коэффициенты метрик сравнения.
     * Учитываются при расчете финальной оценки
     */
    private val LCS_SCORE_WEIGHT = 0.40
    private val DEMERAU_SCORE_WEIGHT = 0.30
    private val JACCARD_SCORE_WEIGHT = 0.15
    private val K_GRAM_SCORE_WEIGHT = 0.15

    /**
     * Наибольшая общая подпоследовательность (LCS).
     * Определяет длину самой длинной подпоследовательности лексем, которая встречается в обоих кодах
     */
    fun longestCommonSubsequence(tokens1: List<String>, tokens2: List<String>): Int {
        val dp = Array(tokens1.size + 1) { IntArray(tokens2.size + 1) }
        for (i in 1..tokens1.size) {
            for (j in 1..tokens2.size) {
                dp[i][j] = if (tokens1[i - 1] == tokens2[j - 1]) {
                    dp[i - 1][j - 1] + 1
                } else {
                    max(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }
        return dp[tokens1.size][tokens2.size]
    }

    fun scoreLongestCommonSubsequence(tokens1: List<String>, tokens2: List<String>): Double {
        return longestCommonSubsequence(tokens1, tokens2).toDouble() / (max(tokens1.size, tokens2.size))
    }

    /**
     * Расстояние Дамерау-Левенштейна.
     * Количество минимальных операций (вставка, удаление, замена, транспозиция), чтобы превратить одну последовательность лексем в другую
     */
    fun damerauLevenshteinDistance(tokens1: List<String>, tokens2: List<String>): Int {
        val dp = Array(tokens1.size + 1) { IntArray(tokens2.size + 1) }
        for (i in 0..tokens1.size) dp[i][0] = i
        for (j in 0..tokens2.size) dp[0][j] = j

        for (i in 1..tokens1.size) {
            for (j in 1..tokens2.size) {
                val cost = if (tokens1[i - 1] == tokens2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost
                )
                if (i > 1 && j > 1 && tokens1[i - 1] == tokens2[j - 2] && tokens1[i - 2] == tokens2[j - 1]) {
                    dp[i][j] = min(dp[i][j], dp[i - 2][j - 2] + cost)
                }
            }
        }
        return dp[tokens1.size][tokens2.size]
    }

    fun scoreDamerauLevenshteinDistance(tokens1: List<String>, tokens2: List<String>): Double {
        return 1.0 - damerauLevenshteinDistance(tokens1, tokens2).toDouble() / (max(tokens1.size, tokens2.size))
    }

    /**
     * Коэффициент Жаккара.
     * Показывает, насколько похожи множества лексем двух кодов
     * Отношение размера объединения 2-х множеств к количеству общих элементов 2-х множеств
     */
    fun jaccardSimilarity(tokens1: List<String>, tokens2: List<String>): Double {
        val set1 = tokens1.toSet()
        val set2 = tokens2.toSet()
        val intersection = (set1 intersect set2).size.toDouble()
        val union = (set1 union set2).size.toDouble()
        return if (union == 0.0) 0.0 else intersection / union
    }

    /**
     *  Разбиение на K-граммы (Fingerprint Hashing)
     *  Разбивает код на последовательности длины k (например, 3-граммы).
     */
    fun kGrams(tokens: List<String>, k: Int): Set<String> {
        return tokens.windowed(k, 1, partialWindows = false).map { it.joinToString(" ") }.toSet()
    }

    fun kGramSimilarity(tokens1: List<String>, tokens2: List<String>, k: Int): Double {
        val grams1 = kGrams(tokens1, k)
        val grams2 = kGrams(tokens2, k)
        val intersection = grams1.intersect(grams2).size.toDouble()
        val union = grams1.union(grams2).size.toDouble()
        return if (union == 0.0) 0.0 else intersection / union
    }

    /**
     * Подробные результаты анализа в текстовом формате
     */
    fun analyze(tokens1: List<String>, tokens2: List<String>, k: Int = 3): Map<String, Double> {
        return mapOf(
            "Source file lexemes count" to tokens1.size.toDouble(),
            "Comparing file lexemes count" to tokens2.size.toDouble(),
            "LCS Length" to longestCommonSubsequence(tokens1, tokens2).toDouble(),
            "LCS Similarity" to scoreLongestCommonSubsequence(tokens1, tokens2),
            "Damerau-Levenshtein Distance" to damerauLevenshteinDistance(tokens1, tokens2).toDouble(),
            "Damerau-Levenshtein Similarity" to scoreDamerauLevenshteinDistance(tokens1, tokens2),
            "Jaccard Similarity" to jaccardSimilarity(tokens1, tokens2),
            "K-Gram Similarity (k=$k)" to kGramSimilarity(tokens1, tokens2, k)
        )
    }

    /**
     * Получение итоговой оценки с учетом всех метрик. их весов, а также размера файла
     */
    fun finalScore(tokens1: List<String>, tokens2: List<String>, k: Int = 3): Double {
        val lcs = scoreLongestCommonSubsequence(tokens1, tokens2)
        val demerau = scoreDamerauLevenshteinDistance(tokens1, tokens2)
        val jaccard = jaccardSimilarity(tokens1, tokens2)
        val gram = kGramSimilarity(tokens1, tokens2, k)

        val baseScore = lcs * LCS_SCORE_WEIGHT + demerau * DEMERAU_SCORE_WEIGHT + jaccard * JACCARD_SCORE_WEIGHT + gram * K_GRAM_SCORE_WEIGHT

        return baseScore
    }
}

fun main2() {
    val parser = TokenParser()

    val tokens1 = parser.parseCode(CodeSnippets.shortCode())
    val tokens2 = parser.parseCode(CodeSnippets.shortCodeBitChanged())
    val tokens3 = parser.parseCode(CodeSnippets.longCode())

    val tokenAnalyzer = TokenAnalyzer()

    val result = tokenAnalyzer.analyze(tokens1, tokens2)
    val result2 = tokenAnalyzer.analyze(tokens1, tokens3)

    println("\ncode1 and code2")
    result.forEach { (metric, value) -> println("$metric: $value") }
    println(tokenAnalyzer.finalScore(tokens1, tokens2))

    println("\ncode1 and code3")
    result2.forEach { (metric, value) -> println("$metric: $value") }
    println(tokenAnalyzer.finalScore(tokens1, tokens3))
}