package edu.plag.core.parser

import JavaLexer
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.springframework.stereotype.Component
import java.io.StringReader

@Component
class TokenParser {

    /**
     * Парсит Java-код и разделяет на отдельные токены
     */
    fun parseTokens(sourceCode: String): List<String> {
        val lexer = JavaLexer(CharStreams.fromReader(StringReader(sourceCode)))
        val tokenStream = CommonTokenStream(lexer)
        tokenStream.fill() // Заполняем поток токенами

        return tokenStream.tokens
            .filter { it.type != Token.EOF && it.type != JavaLexer.WS } // Убираем пробелы
            .map { normalizeToken(it) }
    }


    /**
     * Нормализует вид токена
     */
    private fun normalizeToken(token: Token): String {
        return when (token.type) {
            // TODO: (AFTER ALL) подумать что еще заменять при нормализации
            JavaLexer.LINE_COMMENT, JavaLexer.COMMENT -> "" // Убираем комментарии
            // JavaLexer.IDENTIFIER -> "VAR" // Нормализуем все идентификаторы
            JavaLexer.DECIMAL_LITERAL, JavaLexer.FLOAT_LITERAL, JavaLexer.STRING_LITERAL -> "CONST" // Нормализуем литералы
            else -> token.text // Оставляем ключевые слова и операторы
        }
    }
}
