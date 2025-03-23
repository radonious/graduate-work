package edu.plag.analysis.parser

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.IfStmt
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge


/**
 * Парсит Java-код и строит AST (шаг 1)
 */
fun parseAst(code: String): CompilationUnit {
    return StaticJavaParser.parse(code)
}

/**
 * Фильтрует AST и удаляет ненужную информацию (шаг 2)
 */
fun filterAst(cu: CompilationUnit): CompilationUnit {
    cu.allComments.clear()
    cu.imports.clear()
    return cu
}

/**
 * Конвертирует AST в граф (шаг 3)
 */
fun convert(node: Node): DefaultDirectedGraph<String, DefaultEdge> {
    val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
    addNodeToGraph(node, graph)
    return graph
}

// TODO: сделать ноды графа менее (но достаточно информативными). Сейчас в них много ненужной информации
private fun addNodeToGraph(node: Node, graph: DefaultDirectedGraph<String, DefaultEdge>) {
    val nodeId = when (node) {
        is MethodDeclaration -> "Method_${node.nameAsString}"
        is IfStmt -> "If_${node.condition.toString().hashCode()}"
        is ExpressionStmt -> "Expr_${node.expression.toString().hashCode()}"
        else -> "${node.javaClass.simpleName}_${node.hashCode()}" // Общий случай
    }

    graph.addVertex(nodeId)
    node.childNodes.forEach { child ->
        val childId = when (child) {
            is MethodDeclaration -> "Method_${child.nameAsString}"
            is IfStmt -> "If_${child.condition.toString().hashCode()}"
            is ExpressionStmt -> "Expr_${child.expression.toString().hashCode()}"
            else -> "${child.javaClass.simpleName}_${child.hashCode()}"
        }
        graph.addVertex(childId)
        graph.addEdge(nodeId, childId)
        addNodeToGraph(child, graph) // Рекурсия
    }
}

/**
 * Полная цепочка преобразований текста программы в граф (шаг 1-3)
 */
fun codeToGraph(code: String): DefaultDirectedGraph<String, DefaultEdge> {
    val ast = parseAst(code)
    val filtered = filterAst(ast)
    return convert(filtered)
}

