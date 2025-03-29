package edu.plag.analysis.parser

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit

class AstParser {
    /**
     * Парсит Java-код и строит AST (шаг 1)
     */
    fun parseAst(code: String): CompilationUnit {
        val ast = StaticJavaParser.parse(code)
        ast.allComments.clear()
        ast.imports.clear()
        return ast
    }
}

