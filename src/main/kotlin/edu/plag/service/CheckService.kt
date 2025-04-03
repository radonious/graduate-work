package edu.plag.service

import edu.plag.core.analyzer.LexicalAnalyzer
import edu.plag.core.analyzer.SyntaxAnalyzer
import edu.plag.dto.*
import org.springframework.stereotype.Service
import kotlin.io.path.readText

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

        private const val ACCEPTABLE_LINES_COUNT_DIFF_RATE = 0.5
    }

    fun checkSnippet(userCode: String, settings: CheckSettings): CheckResults {
        val userFileInfo = FileInfo("snippet", "", userCode.lines().size)

        val suspectsLexical = ArrayList<LexicalPair>()
        val suspectsSyntax = ArrayList<SyntaxPair>()

        var checksCounter = 0;
        val startTime = System.nanoTime()

        fileService.getAllFiles().forEach {
            // TODO: (PRIORITY) продумать настройки и учесть их
            val dbFileInfo = FileInfo.from(it)
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

        val snippetCheck = SingleCheckResults(
            source = userFileInfo,
            filesSuspectedByLexicalAnalyzer = suspectsLexical,
            filesSuspectedBySyntaxAnalyzer = suspectsSyntax,
            plagiarism = plagiarism,
        )

        val common = CommonCheckResults(
            duration = checkDuration,
            checks = checksCounter,
            plagiarism = plagiarism, // TODO: (PRIORITY) у проекта должен считаться у четом размера файлов и их плагиата
        )

        return CheckResults(
            common = common,
            checks = listOf(snippetCheck),
            settings = settings,
        )
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

        return LEXICAL_ANALYSIS_WEIGHT * (lexicalAccum / lexicalLines) + SYNTAX_ANALYSIS_WEIGHT * (syntaxAccum / syntaxLines)
    }

    // TODO: (PRIORITY ASAP) реализовать
    fun checkFile(userCode: String, settings: CheckSettings): CheckResults {
        TODO()
    }

    fun checkArchive() {
        TODO()
    }
}