package edu.plag.util

class CodeSnippets {
    companion object {
        fun shortCode() = """
            public class Test {
                public static void main(String[] args) {
                    int x = 10.0;
                    String text = "Hello, World!";
                    System.out.println(text);
                }
            }
        """.trimIndent()

        fun shortCodeBitChanged() = """
            public class Test2 {
                public static void main(String[] args) {
                    int x = 10.0;
                    String text = "Hello, World LOL!";
                    System.out.println(text + "Sasha");
                }
            }
        """.trimIndent()

        fun longCode() = """
            package edu.plag.analysis.analyzer

            import edu.plag.analysis.parser.TokenParser
            import kotlin.math.*

            class TokenAnalyzer {

                /**
                 * Весовые коэффициенты метрик сравнения.
                 * Учитываются при расчете финальной оценки
                 */
                private val LCS_SCORE_WEIGHT = 0.35
                private val DEMERAU_SCORE_WEIGHT = 0.25
                private val JACCARD_SCORE_WEIGHT = 0.10
                private val COSINE_SCORE_WEIGHT = 0.20
                private val K_GRAM_SCORE_WEIGHT = 0.10

                /**
                 * Значения для изменения оценки в зависимости от размера кода
                 */
                private val CODE_SIZE_SCORE_WEIGHT = 0.70
                private val alpha = 10
                
                
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
                 * Косинусное сходство.
                 * Строит векторное представление частот лексем и измеряет косинус угла между ними
                 */
                fun cosineSimilarity(tokens1: List<String>, tokens2: List<String>): Double {
                    val freq1 = tokens1.groupingBy { it }.eachCount()
                    val freq2 = tokens2.groupingBy { it }.eachCount()

                    val allTokens = freq1.keys union freq2.keys
                    val dotProduct = allTokens.sumOf { (freq1[it] ?: 0) * (freq2[it] ?: 0) }.toDouble()

                    val magnitude1 = sqrt(freq1.values.sumOf { it * it }.toDouble())
                    val magnitude2 = sqrt(freq2.values.sumOf { it * it }.toDouble())

                    return if (magnitude1 == 0.0 || magnitude2 == 0.0) 0.0 else dotProduct / (magnitude1 * magnitude2)
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
                        "LCS Length Score" to scoreLongestCommonSubsequence(tokens1, tokens2),
                        "Damerau-Levenshtein Distance" to scoreDamerauLevenshteinDistance(tokens1, tokens2),
                        "Jaccard Similarity" to jaccardSimilarity(tokens1, tokens2),
                        "Cosine Similarity" to cosineSimilarity(tokens1, tokens2),
                        "K-Gram Similarity (k=)" to kGramSimilarity(tokens1, tokens2, k)
                    )
                }

                /**
                 * Получение итоговой оценки с учетом всех метрик. их весов, а также размера файла
                 */
                fun finalScore(tokens1: List<String>, tokens2: List<String>, k: Int = 3): Double {
                    val lcs = scoreLongestCommonSubsequence(tokens1, tokens2)
                    val demerau = scoreDamerauLevenshteinDistance(tokens1, tokens2)
                    val jaccard = jaccardSimilarity(tokens1, tokens2)
                    val cosine = cosineSimilarity(tokens1, tokens2)
                    val gram = kGramSimilarity(tokens1, tokens2, k)

                    val baseScore = lcs * LCS_SCORE_WEIGHT + demerau * DEMERAU_SCORE_WEIGHT + jaccard * JACCARD_SCORE_WEIGHT + cosine * COSINE_SCORE_WEIGHT + gram * K_GRAM_SCORE_WEIGHT
                    val codeSizeScore = baseScore * (1.0 - exp(-tokens1.size.toDouble() / 10))

                    println("base score:\n code score:")
                    val finalScore = CODE_SIZE_SCORE_WEIGHT * baseScore + (1 - CODE_SIZE_SCORE_WEIGHT) * codeSizeScore

                    return finalScore
                }
            }

            fun main() {
                val parser = TokenParser()
                val code1 = ""${'"'}
                    public class Test {
                        public static void main(String[] args) {
                            int x = 10.0;
                            String text = "Hello, World!";
                            System.out.println(text);
                        }
                    }
                ""${'"'}.trimIndent()

                val code2 = ""${'"'}
                    public class Test2 {
                        public static void main(String[] args) {
                            int x = 10.0;
                            String text = "Hello, World LOL!";
                            System.out.println(text + "Sasha");
                        }
                    }
                ""${'"'}.trimIndent()

                val code3 = ""${'"'}
                    public class Test {
                        public static void main(String[] args) {
                            int x = 10.0;
                            String text = "Hello, World!";
                            System.out.println(text);
                        }
                    }
                ""${'"'}.trimIndent()

                val tokens1 = parser.parseCode(code1)
                val tokens2 = parser.parseCode(code2)
                val tokens3 = parser.parseCode(code3)

                println(tokens1.joinToString(" "))
                println(tokens2.joinToString(" "))
                println(tokens3.joinToString(" "))

                val tokenAnalyzer = TokenAnalyzer()

                val result = tokenAnalyzer.analyze(tokens1, tokens2)
                val result2 = tokenAnalyzer.analyze(tokens1, tokens3)

                println("\ncode1 and code2")
                result.forEach { (metric, value) -> println("") }
                println(tokenAnalyzer.finalScore(tokens1, tokens2))

                println("\ncode1 and code3")
                result2.forEach { (metric, value) -> println("") }
                println(tokenAnalyzer.finalScore(tokens1, tokens3))
            }
        """.trimIndent()
    }
}