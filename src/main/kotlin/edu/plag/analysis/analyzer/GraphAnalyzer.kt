package edu.plag.analysis.analyzer

import com.github.javaparser.ast.Node
import edu.plag.analysis.converter.AstToGraphConverter
import edu.plag.analysis.parser.parseAst
import edu.plag.util.CodeSnippets
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.nio.dot.DOTExporter
import java.io.FileWriter


class GraphAnalyzer {

    fun areGraphsIsomorphic(graph1: SimpleGraph<Node, DefaultEdge>, graph2: SimpleGraph<Node, DefaultEdge>): Boolean {
        val inspector = VF2GraphIsomorphismInspector(graph1, graph2)
        return inspector.isomorphismExists()
    }

    fun findIsomorphicSubgraphs(
        graph1: SimpleGraph<Node, DefaultEdge>, graph2: SimpleGraph<Node, DefaultEdge>
    ): Boolean {
        val inspector = VF2SubgraphIsomorphismInspector(graph1, graph2)
        return inspector.isomorphismExists()
    }

    private fun extractSubgraphs(
        graph: DefaultDirectedGraph<String, DefaultEdge>, minSize: Int = 5
    ): List<DefaultDirectedGraph<String, DefaultEdge>> {
        val subgraphs = mutableListOf<DefaultDirectedGraph<String, DefaultEdge>>()
        val visited = mutableSetOf<String>()

        fun dfs(vertex: String, currentGraph: DefaultDirectedGraph<String, DefaultEdge>) {
            visited.add(vertex)
            currentGraph.addVertex(vertex)

            graph.outgoingEdgesOf(vertex).forEach { edge ->
                val target = graph.getEdgeTarget(edge)
                if (target !in visited) {
                    currentGraph.addVertex(target)
                    currentGraph.addEdge(vertex, target)
                    dfs(target, currentGraph)
                }
            }
        }

        graph.vertexSet().forEach { vertex ->
            if (vertex !in visited) {
                val subgraph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
                dfs(vertex, subgraph)
                if (subgraph.vertexSet().size >= minSize) {
                    subgraphs.add(subgraph)
                }
            }
        }
        return subgraphs
    }

    fun hasIsomorphicSubgraphs(
        graph1: DefaultDirectedGraph<String, DefaultEdge>, graph2: DefaultDirectedGraph<String, DefaultEdge>
    ): Boolean {
        val subgraphs1 = extractSubgraphs(graph1)
        val subgraphs2 = extractSubgraphs(graph2)

        subgraphs1.forEach { sg1 ->
            subgraphs2.forEach { sg2 ->
                val inspector = VF2SubgraphIsomorphismInspector(sg1, sg2)
                if (inspector.isomorphismExists()) {
                    return true
                }
            }
        }
        return false
    }

    fun hasIsomorphicSubgraphs2(
        graph1: DefaultDirectedGraph<String, DefaultEdge>, graph2: DefaultDirectedGraph<String, DefaultEdge>
    ): Boolean {
        val inspector = VF2SubgraphIsomorphismInspector(graph1, graph2)
        return inspector.isomorphismExists()
    }

    fun visualizeSubgraphs(subgraphs: List<DefaultDirectedGraph<String, DefaultEdge>>, prefix: String = "subgraph") {
        subgraphs.forEachIndexed { index, subgraph ->
            val exporter = DOTExporter<String, DefaultEdge> { vertex -> vertex } // Используем ID узла как метку
            val fileName = "${prefix}_$index.dot"
            FileWriter(fileName).use { writer ->
                exporter.exportGraph(subgraph, writer)
            }
            println("Подграф сохранён в $fileName")
        }
    }

    fun visualizeGraph(graph: DefaultDirectedGraph<String, DefaultEdge>, prefix: String = "graph") {
        val exporter = DOTExporter<String, DefaultEdge> { vertex -> vertex }
        val fileName = "${prefix}.dot"
        FileWriter(fileName).use { writer ->
            exporter.exportGraph(graph, writer)
        }
        println("Подграф сохранён в $fileName")
    }
}

fun main() {
    val code1 = CodeSnippets.shortCode()

    val code2 = CodeSnippets.shortCodeBitChanged()

    val ast1 = parseAst(code1)
    val ast2 = parseAst(code2)

    val graph1 = AstToGraphConverter().convert(ast1)
    val graph2 = AstToGraphConverter().convert(ast2)

   GraphAnalyzer().visualizeGraph(graph1, "1")
   GraphAnalyzer().visualizeGraph(graph1, "2")

    val hasPlagiarism = GraphAnalyzer().hasIsomorphicSubgraphs2(graph1, graph2)
    println("Заимствования найдены: $hasPlagiarism")
}