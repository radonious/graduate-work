package edu.plag.analysis.analyzer

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.*
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import edu.plag.analysis.parser.AstParser
import edu.plag.util.CodeSnippets
import org.jgrapht.Graph
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector
import org.jgrapht.graph.AbstractBaseGraph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import kotlin.math.max
import kotlin.math.min


class AstAnalyzer {
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
     * Конвертирует AST в граф
     */
    fun buildGraph(unit: CompilationUnit): DefaultDirectedGraph<String, DefaultEdge> {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        val stack = ArrayDeque<String>()
        val counters = HashMap<String, Int>()

        unit.accept(object : VoidVisitorAdapter<Void>() {

            private fun handleNode(prefix: String, childProcessor: () -> Unit) {

                counters[prefix] = counters.getOrDefault(prefix, 0) + 1

                val label = "${prefix}_${counters[prefix]}"

                // Добавляем вершину и связи
                if (!graph.containsVertex(label)) {
                    graph.addVertex(label)
                }

                // Связь с родителем
                stack.lastOrNull()?.let { parent ->
                    graph.addEdge(parent, label)
                }

                stack.addLast(label)
                childProcessor()
                stack.removeLast()
            }

            override fun visit(n: ClassOrInterfaceDeclaration, arg: Void?) {
                handleNode("CLASS") { super.visit(n, arg) }
            }

            override fun visit(n: MethodDeclaration, arg: Void?) {
                handleNode("METHOD") { super.visit(n, arg) }
            }

            override fun visit(n: ForStmt, arg: Void?) {
                handleNode("FOR") { super.visit(n, arg) }
            }

            override fun visit(n: AssignExpr, arg: Void?) {
                handleNode("ASSIGN") { super.visit(n, arg) }
            }

            override fun visit(n: VariableDeclarationExpr, arg: Void?) {
                handleNode("VAR") { super.visit(n, arg) }
            }

            override fun visit(n: MethodCallExpr, arg: Void?) {
                handleNode("METHOD_CALL") { super.visit(n, arg) }
            }

            override fun visit(n: LambdaExpr, arg: Void?) {
                handleNode("LAMBDA") { super.visit(n, arg) }
            }

            override fun visit(n: WhileStmt, arg: Void?) {
                handleNode("WHILE") { super.visit(n, arg) }
            }

            override fun visit(n: DoStmt, arg: Void?) {
                handleNode("DO_WHILE") { super.visit(n, arg) }
            }

            override fun visit(n: IfStmt, arg: Void?) {
                handleNode("IF") { super.visit(n, arg) }
            }

            override fun visit(n: SwitchStmt, arg: Void?) {
                handleNode("SWITCH") { super.visit(n, arg) }
            }

            override fun visit(n: ForEachStmt, arg: Void?) {
                handleNode("FOR_EACH") { super.visit(n, arg) }
            }

            override fun visit(n: ReturnStmt, arg: Void?) {
                handleNode("RETURN") { super.visit(n, arg) }
            }

            override fun visit(n: TryStmt, arg: Void?) {
                handleNode("TRY") { super.visit(n, arg) }
            }

            override fun visit(n: ThrowStmt, arg: Void?) {
                handleNode("THROW") { super.visit(n, arg) }
            }

            override fun visit(n: AssertStmt, arg: Void?) {
                handleNode("ASSERT") { super.visit(n, arg) }
            }

            override fun visit(n: SynchronizedStmt, arg: Void?) {
                handleNode("SYNCHRONIZED") { super.visit(n, arg) }
            }

            override fun visit(n: DoubleLiteralExpr, arg: Void?) {
                handleNode("DOUBLE_LIT") { super.visit(n, arg) }
            }

            override fun visit(n: NullLiteralExpr, arg: Void?) {
                handleNode("NULL_LIT") { super.visit(n, arg) }
            }

            override fun visit(n: IntegerLiteralExpr, arg: Void?) {
                handleNode("INT_LIT") { super.visit(n, arg) }
            }

            override fun visit(n: StringLiteralExpr, arg: Void?) {
                handleNode("STRING_LIT") { super.visit(n, arg) }
            }

            override fun visit(n: BooleanLiteralExpr, arg: Void?) {
                handleNode("BOOL_LIT") { super.visit(n, arg) }
            }

            override fun visit(n: TextBlockLiteralExpr, arg: Void?) {
                handleNode("TEXT_LIT") { super.visit(n, arg) }
            }

            override fun visit(n: CharLiteralExpr, arg: Void?) {
                handleNode("TEXT_LIT") { super.visit(n, arg) }
            }

            override fun visit(n: LongLiteralExpr, arg: Void?) {
                handleNode("LONG_LIT") { super.visit(n, arg) }
            }

        }, null)

        return graph
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

    fun editDistanceSiilarity(graph1: Graph<String, DefaultEdge>, graph2: Graph<String, DefaultEdge>): Double {
        val ged = calculateEditDistance(graph1, graph2)
        val maxGed = calculateMaxPossibleDistance(graph1, graph2)
        return if (maxGed == 0.0) 1.0 else 1.0 - (ged / maxGed)
    }

    private fun calculateEditDistance(graph1: Graph<String, DefaultEdge>, graph2: Graph<String, DefaultEdge>): Double {
        val vertexCost: (String?, String?) -> Double = { a, b ->
            when {
                a == null -> 1.0 // Вставка
                b == null -> 1.0 // Удаление
                a == b -> 0.0    // Ничего
                else -> 0.5      // Замена
            }
        }

        val edgeCost: (DefaultEdge?, DefaultEdge?) -> Double = { _, _ -> 0.25 }

        val (smaller, larger) = listOf(graph1, graph2).sortedBy { it.vertexSet().size }
        val m = smaller.vertexSet().size
        val n = larger.vertexSet().size

        return Array(m + 1) { DoubleArray(n + 1) }.apply {
            forEachIndexed { i, row ->
                row.forEachIndexed { j, _ ->
                    this[i][j] = when {
                        i == 0 && j == 0 -> 0.0
                        i == 0 -> this[i][j - 1] + vertexCost(null, larger.vertexSet().elementAt(j - 1))
                        j == 0 -> this[i - 1][j] + vertexCost(smaller.vertexSet().elementAt(i - 1), null)
                        else -> {
                            val v1 = smaller.vertexSet().elementAt(i - 1)
                            val v2 = larger.vertexSet().elementAt(j - 1)
                            min(
                                min(
                                    this[i - 1][j] + vertexCost(v1, null),
                                    this[i][j - 1] + vertexCost(null, v2)
                                ),
                                this[i - 1][j - 1] + vertexCost(v1, v2) +
                                        compareEdges(smaller, larger, v1, v2, edgeCost)
                            )
                        }
                    }
                }
            }
        }[m][n]
    }

    private fun compareEdges(
        graph1: Graph<String, DefaultEdge>,
        graph2: Graph<String, DefaultEdge>,
        v1: String,
        v2: String,
        edgeCost: (DefaultEdge?, DefaultEdge?) -> Double
    ): Double {
        val edges1 = graph1.edgesOf(v1)
        val edges2 = graph2.edgesOf(v2)

        return edges1.sumOf { e1 ->
            edges2.minOf { e2 -> edgeCost(e1, e2) }
        } + (edges2.size - edges1.size) * 0.25
    }

    private fun calculateMaxPossibleDistance(
        graph1: Graph<String, DefaultEdge>,
        graph2: Graph<String, DefaultEdge>
    ): Double {
        val maxNodes = max(graph1.vertexSet().size, graph2.vertexSet().size)
        val maxEdges = max(graph1.edgeSet().size, graph2.edgeSet().size)
        return maxNodes * 1.0 + maxEdges * 0.25
    }


    // Работает хуже
    class ASTEditDistanceCalculator(
        private val vertexCost: Double = 1.0,
        private val edgeCost: Double = 0.2
    ) {
        fun computeDistance(
            graph1: DefaultDirectedGraph<String, DefaultEdge>,
            graph2: DefaultDirectedGraph<String, DefaultEdge>
        ): Double {
            val root1 = findRoot(graph1)
            val root2 = findRoot(graph2)

            val editDistance = computeTreeEditDistance(root1, root2, graph1, graph2)

            val maxNodes = max(graph1.vertexSet().size, graph2.vertexSet().size).toDouble()
            val maxEdges = max(graph1.edgeSet().size, graph2.edgeSet().size).toDouble()

            val maxPossibleEdits = maxNodes * vertexCost + maxEdges * edgeCost
            return 1.0 - (editDistance / maxPossibleEdits)

        }

        private fun findRoot(graph: Graph<String, DefaultEdge>): String? {
            return graph.vertexSet().find { graph.inDegreeOf(it) == 0 }
        }

        private fun computeTreeEditDistance(
            node1: String?,
            node2: String?,
            graph1: DefaultDirectedGraph<String, DefaultEdge>,
            graph2: DefaultDirectedGraph<String, DefaultEdge>
        ): Double {
            return when {
                node1 == null && node2 == null -> 0.0
                node1 == null -> insertCost(node2!!, graph2)
                node2 == null -> deleteCost(node1, graph1)
                else -> {
                    val labelCost = if (node1 != node2) vertexCost else 0.0
                    val childrenCost = computeChildrenEditDistance(
                        getOrderedChildren(node1, graph1),
                        getOrderedChildren(node2, graph2),
                        graph1,
                        graph2
                    )
                    labelCost + childrenCost
                }
            }
        }

        private fun insertCost(node: String, graph: DefaultDirectedGraph<String, DefaultEdge>): Double {
            var cost = vertexCost
            getOrderedChildren(node, graph).forEach { child ->
                cost += insertCost(child, graph) + edgeCost
            }
            return cost
        }

        private fun deleteCost(node: String, graph: DefaultDirectedGraph<String, DefaultEdge>): Double {
            var cost = vertexCost
            getOrderedChildren(node, graph).forEach { child ->
                cost += deleteCost(child, graph) + edgeCost
            }
            return cost
        }

        private fun getOrderedChildren(
            node: String,
            graph: DefaultDirectedGraph<String, DefaultEdge>
        ): List<String> {
            return graph.outgoingEdgesOf(node).map { graph.getEdgeTarget(it) }
        }

        private fun computeChildrenEditDistance(
            children1: List<String>,
            children2: List<String>,
            graph1: DefaultDirectedGraph<String, DefaultEdge>,
            graph2: DefaultDirectedGraph<String, DefaultEdge>
        ): Double {
            val m = children1.size
            val n = children2.size
            val dp = Array(m + 1) { DoubleArray(n + 1) }

            for (i in 0..m) {
                for (j in 0..n) {
                    dp[i][j] = when {
                        i == 0 && j == 0 -> 0.0
                        i == 0 -> dp[0][j - 1] + insertCost(children2[j - 1], graph2)
                        j == 0 -> dp[i - 1][0] + deleteCost(children1[i - 1], graph1)
                        else -> minOf(
                            dp[i - 1][j] + deleteCost(children1[i - 1], graph1),
                            dp[i][j - 1] + insertCost(children2[j - 1], graph2),
                            dp[i - 1][j - 1] + computeTreeEditDistance(
                                children1[i - 1],
                                children2[j - 1],
                                graph1,
                                graph2
                            ) + edgeCost
                        )
                    }
                }
            }
            return dp[m][n]
        }
    }

    // Работает лучше
    class GraphEditDistanceCalculator(
        private val vertexCost: Double = 1.0,
        private val edgeCost: Double = 0.2,
    ) {
        fun computeSimilarity(
            graph1: DefaultDirectedGraph<String, DefaultEdge>,
            graph2: DefaultDirectedGraph<String, DefaultEdge>
        ): Double {
            val editDistance = computeEditDistance(graph1, graph2)

            val maxVertexes = max(graph1.vertexSet().size, graph2.vertexSet().size).toDouble()
            val maxEdges = max(graph1.edgeSet().size, graph2.edgeSet().size).toDouble()

            val maxPossibleEdits = maxVertexes * vertexCost + maxEdges * edgeCost
            val result = max(1.0 - (editDistance / maxPossibleEdits), 0.0)
            return min(result, 1.0)
        }

        private fun computeEditDistance(
            graph1: DefaultDirectedGraph<String, DefaultEdge>,
            graph2: DefaultDirectedGraph<String, DefaultEdge>
        ): Double {
            val vertices1 = graph1.vertexSet()
            val vertices2 = graph2.vertexSet()

            // 1. Подсчёт разницы по вершинам
            val unmatchedVertices = vertices1.symmetricDifference(vertices2)
            val vertexEditCost = unmatchedVertices.size * vertexCost

            // 2. Подсчёт разницы по рёбрам
            val edgePairs1 = graph1.edgeSet().map { e -> graph1.getEdgeSource(e) to graph1.getEdgeTarget(e) }.toSet()
            val edgePairs2 = graph2.edgeSet().map { e -> graph2.getEdgeSource(e) to graph2.getEdgeTarget(e) }.toSet()

            val unmatchedEdges = edgePairs1.symmetricDifference(edgePairs2)
            val edgeEditCost = unmatchedEdges.size * edgeCost

            return vertexEditCost + edgeEditCost
        }

        private fun <T> Set<T>.symmetricDifference(other: Set<T>): Set<T> {
            return (this union other) - (this intersect other)
        }
    }

}

// TODO: убрать после создания сервиса 2
fun main33() {
    // Предположительный код сервиса
    val userCode = CodeSnippets.longCode()
    val dbCode = CodeSnippets.canonicalBSearch()

    val parser = AstParser()
    val analyzer = AstAnalyzer()

    val ast1 = parser.parseAst(userCode)
    val ast2 = parser.parseAst(dbCode)

    val graph1 = analyzer.buildGraph(ast1)
    val graph2 = analyzer.buildGraph(ast2)

    val plag = analyzer.hasAstPlagiarism(graph1, graph2, 9)

    println("Found plag: $plag")

    println("\nUsers")
    println("Vertices: ${graph1.vertexSet()}")
    println("Edges: ${graph1.edgeSet()}\n")

    println("\nDB")
    println("Vertices: ${graph2.vertexSet()}")
    println("Edges: ${graph2.edgeSet()}\n")

    println(AstAnalyzer.ASTEditDistanceCalculator().computeDistance(graph1, graph2))
    println(AstAnalyzer.GraphEditDistanceCalculator().computeSimilarity(graph1, graph2)) // BEST
    println(AstAnalyzer().editDistanceSiilarity(graph1, graph2))
}