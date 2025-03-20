package edu.plag.analysis.converter

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.stmt.IfStmt
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph


class AstToGraphConverter {
    // ChatGOT
    fun convertAstToGraph(root: Node): SimpleGraph<Node, DefaultEdge> {
        val graph = SimpleGraph<Node, DefaultEdge>(DefaultEdge::class.java)
        addNodesToGraph(root, graph)
        return graph
    }

    private fun addNodesToGraph(node: Node, graph: SimpleGraph<Node, DefaultEdge>) {
        graph.addVertex(node)
        node.childNodes.forEach {
            graph.addVertex(it)
            graph.addEdge(node, it)
            addNodesToGraph(it, graph)
        }
    }

    // Grok
    private fun addNodeToGraph(node: Node, graph: DefaultDirectedGraph<String, DefaultEdge>) {
        val nodeId = when (node) {
            is com.github.javaparser.ast.body.MethodDeclaration -> "Method_${node.nameAsString}"
            is com.github.javaparser.ast.stmt.IfStmt -> "If_${node.condition.toString().hashCode()}"
            is com.github.javaparser.ast.stmt.ExpressionStmt -> "Expr_${node.expression.toString().hashCode()}"
            else -> "${node.javaClass.simpleName}_${node.hashCode()}" // Общий случай
        }

        graph.addVertex(nodeId)
        node.childNodes.forEach { child ->
            val childId = when (child) {
                is com.github.javaparser.ast.body.MethodDeclaration -> "Method_${child.nameAsString}"
                is com.github.javaparser.ast.stmt.IfStmt -> "If_${child.condition.toString().hashCode()}"
                is com.github.javaparser.ast.stmt.ExpressionStmt -> "Expr_${child.expression.toString().hashCode()}"
                else -> "${child.javaClass.simpleName}_${child.hashCode()}"
            }
            graph.addVertex(childId)
            graph.addEdge(nodeId, childId)
            addNodeToGraph(child, graph) // Рекурсия
        }
    }

    fun convert(node: Node): DefaultDirectedGraph<String, DefaultEdge> {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        addNodeToGraph(node, graph)
        return graph
    }
}
