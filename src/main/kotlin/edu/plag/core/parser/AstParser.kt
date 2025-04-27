package edu.plag.core.parser

import com.github.javaparser.JavaParser
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ast.CompilationUnit
import edu.plag.exceptions.InvalidFileTypeException
import org.springframework.stereotype.Component

@Component
class AstParser {
    /**
     * Парсит Java-код и формирует Abstract Syntax Tree
     */
    fun parseAst(code: String): CompilationUnit {
        // Важно указать новую версию Java для работы с новыми конструкциями языка
        val config: ParserConfiguration = ParserConfiguration()
            .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
        val parser = JavaParser(config)
        val result = parser.parse(code)

        return if (result.isSuccessful) {
            result.result.get().also {
                it.allComments.clear()
                it.imports.clear()
            }
        } else {
            throw InvalidFileTypeException("Can not parse or validate received file.")
        }
    }
}
