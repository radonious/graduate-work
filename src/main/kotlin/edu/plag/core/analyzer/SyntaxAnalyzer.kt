package edu.plag.core.analyzer

import edu.plag.core.parser.AstParser
import edu.plag.core.parser.GraphParser
import edu.plag.dto.SyntaxAnalyzerResults
import org.jgrapht.Graph
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector
import org.jgrapht.graph.AbstractBaseGraph
import org.jgrapht.graph.AsSubgraph
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
        private const val ISOMORPHISM_WEIGHT = 0.4
        private const val EDIT_DISTANCE_WEIGHT = 0.6

        private const val VERTEX_EDIT_COST = 1.0
        private const val EDGE_EDIT_COST = 0.2
    }

    /**
     * Проверяем наличие изоморфных подграфов
     */
     fun hasAstPlagiarism(
        userAstGraph: AbstractBaseGraph<String, DefaultEdge>,
        dbAstGraph: AbstractBaseGraph<String, DefaultEdge>,
        minSize: Int = 9
    ): Boolean {
        val dbInvariant = calculateGraphInvariant(dbAstGraph)
        val vertices = userAstGraph.vertexSet().toSet()

        return generatePowerSet(vertices, minSize) { subset ->
            val subgraph = createInducedSubgraph(userAstGraph, subset)
            // Если инварианты не совпадают – сразу пропускаем проверку
            if (calculateGraphInvariant(subgraph) != dbInvariant) {
                false
            } else {
                val inspector = VF2SubgraphIsomorphismInspector(
                    dbAstGraph,
                    subgraph,
                    { a, b -> a.substringBefore("_").compareTo(b.substringBefore("_")) },
                    null
                )
                inspector.isomorphismExists()
            }
        }
    }

    private fun calculateGraphInvariant(graph: Graph<String, DefaultEdge>): Int {
        return graph.vertexSet()
            .map { vertex -> graph.inDegreeOf(vertex) + graph.outDegreeOf(vertex) }
            .sorted()
            .hashCode()
    }

    /**
     * Все комбинации элементов (без перестановок)
     */
    // TODO: (LATER) переделать метод, если на большой базе будут долгие проверки после оптимизаций
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

    /**
     * Сделать подграф по определенным вершинам графа
     */
    private fun <V> createInducedSubgraph(
        graph: Graph<V, DefaultEdge>,
        vertices: Set<V>
    ): Graph<V, DefaultEdge> {
        return AsSubgraph(graph, vertices)
    }

    fun scoreEditDistance(
        graph1: DefaultDirectedGraph<String, DefaultEdge>,
        graph2: DefaultDirectedGraph<String, DefaultEdge>,
        editDistance: Double,
    ): Double {
        val maxVertexes = max(graph1.vertexSet().size, graph2.vertexSet().size)
        val maxEdges = max(graph1.edgeSet().size, graph2.edgeSet().size)
        val maxPossibleEdits = maxVertexes * VERTEX_EDIT_COST + maxEdges * EDGE_EDIT_COST

        if (maxPossibleEdits == 0.0) return 0.0
        return min(max(1.0 - (editDistance / maxPossibleEdits), 0.0), 1.0)
    }

    fun editDistance(
        graph1: DefaultDirectedGraph<String, DefaultEdge>, graph2: DefaultDirectedGraph<String, DefaultEdge>
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
     * Подробные результаты анализа
     */
    fun analyze(userCode: String, dbCode: String): SyntaxAnalyzerResults {
        val userAst = astParser.parseAst(userCode)
        val dbAst = astParser.parseAst(dbCode)

        val userGraph = graphParser.parseGraph(userAst)
        val dbGraph = graphParser.parseGraph(dbAst)

        // TODO: (AFTER ALL) высчитывать оптимальный  minSize динамически
        val hasIsomorphism = hasAstPlagiarism(userGraph, dbGraph, 9)
        val editDistance = editDistance(userGraph, dbGraph)
        val editDistanceScore = scoreEditDistance(userGraph, dbGraph, editDistance)

        val finalScore = editDistanceScore * EDIT_DISTANCE_WEIGHT + if (hasIsomorphism) ISOMORPHISM_WEIGHT else 0.0

        return SyntaxAnalyzerResults(
            hasIsomorphism = hasIsomorphism,
            editDistance = editDistance,
            editDistanceScore = editDistanceScore,
            finalScore = finalScore,
        )
    }
}
