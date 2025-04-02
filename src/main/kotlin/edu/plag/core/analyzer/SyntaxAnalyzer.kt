package edu.plag.core.analyzer

import edu.plag.core.parser.AstParser
import edu.plag.core.parser.GraphParser
import edu.plag.dto.SyntaxAnalyzerResults
import org.jgrapht.Graph
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector
import org.jgrapht.graph.AbstractBaseGraph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.springframework.stereotype.Component
import kotlin.math.max
import kotlin.math.min

@Component
class SyntaxAnalyzer(
    private val graphParser: GraphParser,
    private val astParser: AstParser,
) {

    companion object {
        private val VERTEX_EDIT_COST = 1.0
        private val EDGE_EDIT_COST = 0.2
    }

    /**
     * Проверяем наличие изоморфных подграфов
     */
    fun hasAstPlagiarism(
        userAstGraph: AbstractBaseGraph<String, DefaultEdge>,
        dbAstGraph: AbstractBaseGraph<String, DefaultEdge>,
        minSize: Int = 9
    ): Boolean {
        return generatePowerSet(userAstGraph.vertexSet(), minSize) {
            val subgraph = createInducedSubgraph(userAstGraph, it)
            val inspector = VF2SubgraphIsomorphismInspector(
                dbAstGraph,
                subgraph,
                { a, b -> a.substringBefore("_").compareTo(b.substringBefore("_")) },
                null
            )
            if (inspector.isomorphismExists()) {
                println("Vertices: $it")
                println("Edges: ${subgraph.edgeSet()}\n")
                return@generatePowerSet true
            }
            return@generatePowerSet false
        }
    }

    /**
     * Все комбинации элементов (без перестановок)
     */
    private fun <T> generatePowerSet(input: Set<T>, size: Int, action: (Set<T>) -> Boolean): Boolean {
        val n = input.size
        val elements = input.toList()

        if (size > n || size < 0) return false

        for (ind in 0 until n) {
            val subset = mutableSetOf<T>()
            for (curInd in ind until n) {
                subset.add(elements[curInd])
                if (subset.size >= size && action(subset)) {
                    return true
                }
            }
        }

        return false
    }

    fun scoreEditDistance(
        graph1: DefaultDirectedGraph<String, DefaultEdge>,
        graph2: DefaultDirectedGraph<String, DefaultEdge>,
        editDistance: Double,
    ): Double {
        val maxVertexes = max(graph1.vertexSet().size, graph2.vertexSet().size).toDouble()
        val maxEdges = max(graph1.edgeSet().size, graph2.edgeSet().size).toDouble()

        val maxPossibleEdits = maxVertexes * VERTEX_EDIT_COST + maxEdges * EDGE_EDIT_COST
        val result = max(1.0 - (editDistance / maxPossibleEdits), 0.0)
        return min(result, 1.0)
    }

    fun editDistance(
        graph1: DefaultDirectedGraph<String, DefaultEdge>,
        graph2: DefaultDirectedGraph<String, DefaultEdge>
    ): Double {
        val vertices1 = graph1.vertexSet()
        val vertices2 = graph2.vertexSet()

        // 1. Подсчёт разницы по вершинам
        val unmatchedVertices = vertices1.symmetricDifference(vertices2)
        val vertexEditCost = unmatchedVertices.size * VERTEX_EDIT_COST

        // 2. Подсчёт разницы по рёбрам
        val edgePairs1 = graph1.edgeSet().map { e -> graph1.getEdgeSource(e) to graph1.getEdgeTarget(e) }.toSet()
        val edgePairs2 = graph2.edgeSet().map { e -> graph2.getEdgeSource(e) to graph2.getEdgeTarget(e) }.toSet()

        val unmatchedEdges = edgePairs1.symmetricDifference(edgePairs2)
        val edgeEditCost = unmatchedEdges.size * EDGE_EDIT_COST

        return vertexEditCost + edgeEditCost
    }

    private fun <T> Set<T>.symmetricDifference(other: Set<T>): Set<T> {
        return (this union other) - (this intersect other)
    }


    /**
     * Сделать подграф по определенным вершинам графа
     */
    private fun <V> createInducedSubgraph(graph: Graph<V, DefaultEdge>, vertices: Set<V>): Graph<V, DefaultEdge> {
        val subgraph: Graph<V, DefaultEdge> = DefaultDirectedGraph(DefaultEdge::class.java)

        vertices.forEach { subgraph.addVertex(it) }

        graph.edgeSet().forEach { edge ->
            val source = graph.getEdgeSource(edge)
            val target = graph.getEdgeTarget(edge)
            if (source in vertices && target in vertices) {
                subgraph.addEdge(source, target)
            }
        }

        return subgraph
    }

    /**
     * Подробные результаты анализа
     */
    fun analyze(userCode: String, dbCode: String): SyntaxAnalyzerResults {
        val userAst = astParser.parseAst(userCode)
        val dbAst = astParser.parseAst(dbCode)

        val userGraph = graphParser.parseGraph(userAst)
        val dbGraph = graphParser.parseGraph(dbAst)

        // TODO: (AFTER ALL) высчитывать оптимальный размер динамически
        val hasIsomorphism = hasAstPlagiarism(userGraph, dbGraph, 9)
        val editDistance = editDistance(userGraph, dbGraph)
        val editDistanceScore = scoreEditDistance(userGraph, dbGraph, editDistance)

        return SyntaxAnalyzerResults(
            hasIsomorphism = hasIsomorphism,
            editDistance = editDistance,
            editDistanceScore = editDistanceScore,
        )
    }
}
