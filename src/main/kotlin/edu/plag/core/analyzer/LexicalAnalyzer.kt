package edu.plag.core.analyzer

import edu.plag.core.parser.TokenParser
import edu.plag.dto.LexicalAnalyzerResults
import org.springframework.stereotype.Component
import kotlin.math.max
import kotlin.math.min

@Component
class LexicalAnalyzer(
    private val tokenParser: TokenParser,
) {

    /**
     * Весовые коэффициенты метрик сравнения.
     * Учитываются при расчете финальной оценки
     * Сумма весов должна быть равна 1
     */
    companion object {
        private const val LCS_SCORE_WEIGHT = 0.45
        private const val DAMERAU_SCORE_WEIGHT = 0.40
        private const val JACCARD_SCORE_WEIGHT = 0.10
        private const val K_GRAM_SCORE_WEIGHT = 0.05
    }

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

    fun scoreLongestCommonSubsequence(tokens1: List<String>, tokens2: List<String>, result: Int): Double {
        val score = result.toDouble() / (max(tokens1.size, tokens2.size))
        return if (score.isNaN()) 0.0 else score
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

    fun scoreDamerauLevenshteinDistance(tokens1: List<String>, tokens2: List<String>, result: Int): Double {
        val score = 1.0 - result.toDouble() / (max(tokens1.size, tokens2.size))
        return if (score.isNaN()) 0.0 else score
    }

    /**
     * Коэффициент Жаккара.
     * Показывает, насколько похожи множества лексем двух кодов
     * Отношение размера объединения 2-х множеств к количеству общих элементов 2-х множеств
     */
    fun scoreJaccard(tokens1: List<String>, tokens2: List<String>): Double {
        val set1 = tokens1.toSet()
        val set2 = tokens2.toSet()
        val intersection = (set1 intersect set2).size.toDouble()
        val union = (set1 union set2).size.toDouble()
        return if (union == 0.0) 0.0 else intersection / union
    }


    fun kGrams(tokens: List<String>, k: Int): Set<String> {
        return tokens.windowed(k, 1, partialWindows = false).map { it.joinToString(" ") }.toSet()
    }

    /**
     *  Сравнение K-грамм (Fingerprint Hashing)
     *  Разбивает код на последовательности длины k (например, 3-граммы).
     *  Проверяет насколько полученные множества идентичны
     */
    fun scoreKGrams(tokens1: List<String>, tokens2: List<String>, k: Int): Double {
        val grams1 = kGrams(tokens1, k)
        val grams2 = kGrams(tokens2, k)
        val intersection = grams1.intersect(grams2).size.toDouble()
        val union = grams1.union(grams2).size.toDouble()
        return if (union == 0.0) 0.0 else intersection / union
    }

    /**
     * Подробные результаты анализа.
     * Включает в себя результаты всех методов и итоговую оценку
     */
    fun analyze(userCode: String, dbCode: String, k: Int = 3): LexicalAnalyzerResults {
        val userTokens = tokenParser.parseTokens(userCode)
        val dbTokens = tokenParser.parseTokens(dbCode)

        val lcs = longestCommonSubsequence(userTokens, dbTokens)
        val lcsScore = scoreLongestCommonSubsequence(userTokens, dbTokens, lcs)
        val damerau = damerauLevenshteinDistance(userTokens, dbTokens)
        val damerauScore = scoreDamerauLevenshteinDistance(userTokens, dbTokens, damerau)
        val jaccardScore = scoreJaccard(userTokens, dbTokens)
        val kgramScore = scoreKGrams(userTokens, dbTokens, k)
        val finalScore =
            lcsScore * LCS_SCORE_WEIGHT + damerauScore * DAMERAU_SCORE_WEIGHT + jaccardScore * JACCARD_SCORE_WEIGHT + kgramScore * K_GRAM_SCORE_WEIGHT

        return LexicalAnalyzerResults(
            userCodeLexemesCount = userTokens.size,
            dbCodeLexemesCount = dbTokens.size,
            longestCommonSubsequence = lcs,
            longestCommonSubsequenceScore = lcsScore,
            damerauLevenshteinDistance = damerau,
            damerauLevenshteinDistanceScore = damerauScore,
            jaccardSimilarityScore = jaccardScore,
            kGramSimilarityScore = kgramScore,
            finalScore = finalScore,
        )
    }
}
