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
            .filter { it.type !in listOf(JavaLexer.WS, JavaLexer.EOF) }
            .filter { it.type !in listOf(JavaLexer.LINE_COMMENT, JavaLexer.COMMENT) }
            .map { normalizeToken(it) }
    }

    /**
     * Нормализует вид токенов
     */
    private fun normalizeToken(token: Token): String {
        return when (token.type) {
            JavaLexer.IDENTIFIER -> "VAR"
            JavaLexer.DECIMAL_LITERAL, JavaLexer.HEX_LITERAL, JavaLexer.OCT_LITERAL, JavaLexer.BINARY_LITERAL,
            JavaLexer.FLOAT_LITERAL, JavaLexer.HEX_FLOAT_LITERAL -> "NUM_LITERAL"
            JavaLexer.STRING_LITERAL, JavaLexer.CHAR_LITERAL -> "STR_LITERAL"
            JavaLexer.BOOL_LITERAL -> "BOOL_LITERAL"
            JavaLexer.NULL_LITERAL -> "NULL_LITERAL"

            else -> token.text // Оставляем прочие ключевые слова, операторы и конструкции
        }
    }
}
