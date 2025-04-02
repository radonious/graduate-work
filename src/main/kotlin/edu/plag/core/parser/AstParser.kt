package edu.plag.core.parser

import com.github.javaparser.JavaParser
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import edu.plag.exceptions.InvalidFileTypeException
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException

@Component
class AstParser {
    /**
     * Парсит Java-код и формирует Abstract Syntax Tree
     */
    fun parseAst(code: String): CompilationUnit {
        val config: ParserConfiguration = ParserConfiguration()
            .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
        val parser = JavaParser(config)
        val result = parser.parse(code)

        return if (result.isSuccessful) {
            val unit = result.result.get()
            unit.allComments.clear()
            unit.imports.clear()
            unit
        } else {
            throw InvalidFileTypeException("Can not parse file")
        }
    }
}

