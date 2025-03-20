package edu.plag.analysis.parser

import JavaLexer
import edu.plag.util.CodeSnippets
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import java.io.StringReader

fun parseTokens(sourceCode: String): List<String> {
    val lexer = JavaLexer(CharStreams.fromReader(StringReader(sourceCode)))
    val tokens = CommonTokenStream(lexer)
    tokens.fill() // Заполняем поток токенами

    return tokens.tokens
        .filter { it.type != Token.EOF && it.type != JavaLexer.WS } // Убираем пробелы
        .map { normalizeToken(it) }
}

// TODO: сделать возможность включения/отключения нормализации для разных видов лексем
private fun normalizeToken(token: Token): String {
    return when (token.type) {
        JavaLexer.LINE_COMMENT, JavaLexer.COMMENT -> "" // Убираем комментарии
        // JavaLexer.IDENTIFIER -> "VAR" // Нормализуем все идентификаторы
        JavaLexer.DECIMAL_LITERAL, JavaLexer.FLOAT_LITERAL, JavaLexer.STRING_LITERAL -> "CONST" // Нормализуем литералы
        else -> token.text // Оставляем ключевые слова и операторы
    }
}