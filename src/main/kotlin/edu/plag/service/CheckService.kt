package edu.plag.service

import edu.plag.core.analyzer.LexicalAnalyzer
import edu.plag.core.analyzer.SyntaxAnalyzer
import edu.plag.dto.*
import edu.plag.util.FileUtils
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.readText
import kotlin.text.Charsets.UTF_8

@Service
class CheckService(
    private val lexicalAnalyzer: LexicalAnalyzer,
    private val syntaxAnalyzer: SyntaxAnalyzer,
) {

    companion object {
        private const val SYNTAX_ANALYSIS_WEIGHT = 0.65
        private const val LEXICAL_ANALYSIS_WEIGHT = 0.35
    }

    suspend fun checkSnippet(userCode: String, settings: CheckSettings, userFile: FileInfo? = null): CheckResults {
        val userFileInfo = userFile ?: FileInfo.fromSnippet(userCode)

        // Обнаруженные подозрительные файлы
        val suspectsLexical = ArrayList<LexicalPair>()
        val suspectsSyntax = ArrayList<SyntaxPair>()

        var checksCounter = 0
        val startTime = System.nanoTime()

        FileUtils.getAllSavedFiles().forEach {
            val dbFileInfo = FileInfo.fromPath(it)

            // Пройден ли минимальный размер файла для проверки
            if (userFileInfo.lines < settings.minFileLength || dbFileInfo.lines < settings.minFileLength) return@forEach
            // Приемлимо ли отличие в размере сравниваемых файлов
            if (!isAcceptableSize(userFileInfo.lines, dbFileInfo.lines, settings.maxFileLengthDiffRate)) return@forEach

            val dbCode = it.readText()
            ++checksCounter

            // Включен ли лексический анализ (текст)
            if (settings.lexicalAnalysisEnable) {
                val resultLexical = lexicalAnalyzer.analyze(userCode, dbCode)
                // Превышен ли порог плагиата
                if (resultLexical.finalScore >= settings.lexicalPlagiarismThreshold)
                    suspectsLexical.add(LexicalPair(dbFileInfo, resultLexical))
            }

            // Включен ли синтаксический анализ (структура)
            if (settings.syntaxAnalysisEnable) {
                val resultSyntax = syntaxAnalyzer.analyze(userCode, dbCode)
                // Превышен ли порог плагиата
                if (resultSyntax.finalScore >= settings.syntaxPlagiarismThreshold)
                    suspectsSyntax.add(SyntaxPair(dbFileInfo, resultSyntax))
            }
        }

        val plagiarism = calculatePlagiarism(suspectsLexical, suspectsSyntax, settings)

        val checkDuration = ((System.nanoTime() - startTime) / 1_000_000L).toInt()

        val singleCheck = SingleCheckResults(
            source = userFileInfo,
            filesSuspectedByLexicalAnalyzer = suspectsLexical,
            filesSuspectedBySyntaxAnalyzer = suspectsSyntax,
            plagiarism = plagiarism,
        )

        val common = CommonCheckResults(
            duration = checkDuration,
            checks = checksCounter,
            plagiarism = plagiarism,
        )

        return CheckResults(
            common = common,
            checks = listOf(singleCheck),
            settings = settings,
        )
    }

    suspend fun checkFile(file: MultipartFile, settings: CheckSettings): CheckResults {
        val userCode = String(file.bytes, UTF_8)
        val userFileInfo = FileInfo.fromMultipartFile(file)
        return checkSnippet(userCode, settings, userFileInfo)
    }

    suspend fun checkArchive(file: MultipartFile, settings: CheckSettings): CheckResults {
        val tempFile = saveToTempFile(file)
        val prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss-SSS"))

        val start = System.nanoTime()
        val results = coroutineScope {
            ZipFile(tempFile).use { zip ->
                zip.entries().toList()
                    .filter { isValidJavaFile(it) }
                    .map { entry ->
                        async(Dispatchers.Default) {
                            val content = withContext(Dispatchers.IO) {
                                zip.getInputStream(entry).bufferedReader().use { it.readText() }
                            }

                            val fileInfo = FileInfo(
                                filename = entry.name,
                                prefix = prefix,
                                lines = content.lines().size
                            )

                            withContext(Dispatchers.Default) {
                                checkSnippet(content, settings, fileInfo)
                            }
                        }
                    }.awaitAll()
            }
        }

        val duration = ((System.nanoTime() - start) / 1_000_000).toInt()

        tempFile.delete()
        return sumResults(results, duration, settings)
    }

    private fun saveToTempFile(file: MultipartFile): File {
        val tempFile = kotlin.io.path.createTempFile(suffix = ".zip").toFile()
        file.inputStream.use { input -> tempFile.outputStream().use { input.copyTo(it) } }
        return tempFile
    }

    private fun isValidJavaFile(entry: ZipEntry): Boolean {
        return !entry.isDirectory &&
                entry.name.endsWith(".java", true) &&
                !FileUtils.systemEntries.any { entry.name.startsWith(it) || entry.name.contains("/$it") }
    }

    private fun isAcceptableSize(userLines: Int, dbLines: Int, acceptableRate: Double): Boolean {
        val min: Int = (userLines * (1.0 - acceptableRate)).toInt()
        val max: Int = (userLines * (1.0 + acceptableRate)).toInt()
        return dbLines in min..max
    }

    private fun calculatePlagiarism(
        suspectsLexical: ArrayList<LexicalPair>,
        suspectsSyntax: ArrayList<SyntaxPair>,
        settings: CheckSettings,
    ): Double {
        var lexicalLines = 0
        var lexicalAccum = 0.0
        suspectsLexical.forEach {
            lexicalLines += it.file.lines
            lexicalAccum += it.results.finalScore * it.file.lines
        }

        var syntaxLines = 0
        var syntaxAccum = 0.0
        suspectsSyntax.forEach {
            syntaxLines += it.file.lines
            syntaxAccum += it.results.finalScore * it.file.lines
        }

        val lexicalScore =
            if (lexicalAccum == 0.0 || lexicalLines == 0) 0.0 else (lexicalAccum / lexicalLines)
        val syntaxScore =
            if (syntaxAccum == 0.0 || syntaxLines == 0) 0.0 else (syntaxAccum / syntaxLines)

        return if (!settings.lexicalAnalysisEnable && settings.syntaxAnalysisEnable) {
            syntaxScore
        } else if (settings.lexicalAnalysisEnable && !settings.syntaxAnalysisEnable) {
            lexicalScore
        } else {
            lexicalScore * LEXICAL_ANALYSIS_WEIGHT + syntaxScore * SYNTAX_ANALYSIS_WEIGHT
        }
    }

    private fun sumResults(results: List<CheckResults>, duration: Int, settings: CheckSettings): CheckResults {
        var totalChecks = 0
        var totalAccum = 0.0
        var totalLines = 0L
        val checks = ArrayList<SingleCheckResults>()

        results.forEach { checkResult ->
            val fileInfo = checkResult.checks.first().source
            checks.addAll(checkResult.checks)
            totalChecks += checkResult.common.checks
            totalAccum += checkResult.common.plagiarism * fileInfo.lines
            totalLines += fileInfo.lines
        }

        val common = CommonCheckResults(
            duration = duration,
            checks = totalChecks,
            plagiarism = totalAccum / totalLines,
            unique = 1.0 - totalAccum / totalLines
        )

        return CheckResults(
            common = common,
            checks = checks,
            settings = settings,
        )
    }
}
