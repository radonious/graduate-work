package edu.plag.analysis.parser

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.IfStmt
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge

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

