package edu.plag.service

import edu.plag.core.analyzer.LexicalAnalyzer
import edu.plag.core.analyzer.SyntaxAnalyzer
import edu.plag.dto.*
import edu.plag.util.FileUtils
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.readText
import kotlin.text.Charsets.UTF_8

@Service
class CheckService(
    private val fileService: FileService,
    private val lexicalAnalyzer: LexicalAnalyzer,
    private val syntaxAnalyzer: SyntaxAnalyzer,
) {

    companion object {
        // TODO: (AFTER ALL) продумать коэффициенты после тестов
        private const val SYNTAX_ANALYSIS_WEIGHT = 0.40
        private const val LEXICAL_ANALYSIS_WEIGHT = 0.60

        private const val ACCEPTABLE_LINES_COUNT_DIFF_RATE = 0.25
    }

    suspend fun checkSnippet(userCode: String, settings: CheckSettings, userFile: FileInfo? = null): CheckResults {
        val userFileInfo = userFile ?: FileInfo.fromSnippet(userCode)

        val suspectsLexical = ArrayList<LexicalPair>()
        val suspectsSyntax = ArrayList<SyntaxPair>()

        var checksCounter = 0
        val startTime = System.nanoTime()

        fileService.getAllFiles().forEach {
            // TODO: (PRIORITY) продумать настройки и учесть их
            val dbFileInfo = FileInfo.fromPath(it)
            if (!isAcceptableSize(userFileInfo.lines, dbFileInfo.lines)) return@forEach
            val dbCode = it.readText()
            ++checksCounter

            val resultLexical = lexicalAnalyzer.analyze(userCode, dbCode) // Лексика
            suspectsLexical.add(LexicalPair(dbFileInfo, resultLexical))

            val resultSyntax = syntaxAnalyzer.analyze(userCode, dbCode)  // Синтаксис
            suspectsSyntax.add(SyntaxPair(dbFileInfo, resultSyntax))
        }

        val plagiarism = calculatePlagiarism(suspectsLexical, suspectsSyntax)

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

        val start = System.nanoTime()
        val results = coroutineScope {
            ZipFile(tempFile).use { zip ->
                zip.entries().toList()
                    .filter { isValidJavaFile(it) } // Оставляем только нужные файлы
                    .map { entry ->
                        async(Dispatchers.Default) { processEntry(zip, entry, settings) }
                    }.awaitAll() // Ждём выполнения всех задач
            }
        }
        val duration = ((System.nanoTime() - start) / 1_000_000).toInt()

        tempFile.delete()
        return sumResults(results, duration)
    }

    private fun saveToTempFile(file: MultipartFile): File {
        val tempFile = kotlin.io.path.createTempFile(suffix = ".zip").toFile()
        file.inputStream.use { input -> tempFile.outputStream().use { input.copyTo(it) } }
        return tempFile
    }

    private suspend fun processEntry(zip: ZipFile, entry: ZipEntry, settings: CheckSettings): CheckResults {
        return withContext(Dispatchers.IO) {
            val content = zip.getInputStream(entry).bufferedReader().use { it.readText() }
            val fileInfo = FileInfo(
                filename = entry.name,
                prefix = null,
                lines = content.lines().size
            )
            checkSnippet(content, settings, fileInfo)
        }
    }

    private fun isValidJavaFile(entry: ZipEntry): Boolean {
        return !entry.isDirectory &&
                entry.name.endsWith(".java", true) &&
                !FileUtils.systemEntries.any { entry.name.startsWith(it) || entry.name.contains("/$it") }
    }

    private fun isAcceptableSize(userLines: Int, dbLines: Int): Boolean {
        val min: Int = (userLines * (1.0 - ACCEPTABLE_LINES_COUNT_DIFF_RATE)).toInt()
        val max: Int = (userLines * (1.0 + ACCEPTABLE_LINES_COUNT_DIFF_RATE)).toInt()
        return dbLines in min..max
    }

    private fun calculatePlagiarism(
        suspectsLexical: ArrayList<LexicalPair>,
        suspectsSyntax: ArrayList<SyntaxPair>
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
            if (lexicalAccum == 0.0 || lexicalLines == 0) 0.0 else LEXICAL_ANALYSIS_WEIGHT * (lexicalAccum / lexicalLines)
        val syntaxScore =
            if (syntaxAccum == 0.0 || syntaxLines == 0) 0.0 else SYNTAX_ANALYSIS_WEIGHT * (syntaxAccum / syntaxLines)

        return lexicalScore + syntaxScore
    }

    private fun sumResults(results: List<CheckResults>, duration: Int): CheckResults {
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

        val common = results.first().common.copy(
            duration = duration,
            checks = totalChecks,
            plagiarism = totalAccum / totalLines,
            unique = 1.0 - totalAccum / totalLines
        )

        return CheckResults(
            common = common,
            checks = checks,
            settings = results.first().settings
        )
    }
}
