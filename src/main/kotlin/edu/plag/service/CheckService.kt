package edu.plag.service

import edu.plag.core.analyzer.SyntaxAnalyzer
import edu.plag.core.analyzer.LexicalAnalyzer
import edu.plag.core.parser.AstParser
import edu.plag.core.parser.TokenParser
import edu.plag.dto.*
import edu.plag.util.FileUtils
import org.springframework.stereotype.Service
import kotlin.io.path.readText

@Service
class CheckService(
    private val fileService: FileService,
    private val tokenParser: TokenParser,
    private val lexicalAnalyzer: LexicalAnalyzer,
    private val astParser: AstParser,
    private val syntaxAnalyzer: SyntaxAnalyzer,
) {

    fun check(userCode: String, settings: CheckSettings): CheckResults {
        val suspectsLexical = HashMap<FileInfo, LexicalAnalyzerResults>()
        val suspectsSyntax = HashMap<FileInfo, SyntaxAnalyzerResults>()

        fileService.getAllFiles().forEach {
            val fileInfo = FileInfo.from(it)
            if (fileInfo.lines < settings.minFileSize) return@forEach
            val dbCode = it.readText()
            // Лексика
            val resultLexical = lexicalAnalyzer.analyze(userCode, dbCode)
            if (resultLexical.finalScore > settings.lexicalPlagiarismThreshold)
                suspectsLexical[fileInfo] = resultLexical
            // Синтаксис
            val resultSyntax = syntaxAnalyzer.analyze(userCode, dbCode)
            if (resultSyntax.hasIsomorphism || resultSyntax.editDistanceScore > settings.syntaxPlagiarismThreshold)
                suspectsSyntax[fileInfo] = resultSyntax
        }

        return CheckResults(
            filesSuspectedByLexicalAnalyzer = suspectsLexical,
            filesSuspectedBySyntaxAnalyzer = suspectsSyntax
        )
    }
}