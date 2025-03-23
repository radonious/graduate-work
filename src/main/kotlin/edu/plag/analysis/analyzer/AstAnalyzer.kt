package edu.plag.analysis.analyzer

import com.github.javaparser.ast.Node
import edu.plag.analysis.parser.codeToGraph
import edu.plag.util.CodeSnippets
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph

fun hasIsomorphicStringDDG(
    graph1: DefaultDirectedGraph<String, DefaultEdge>,
    graph2: DefaultDirectedGraph<String, DefaultEdge>
): Boolean {
    val inspector = VF2SubgraphIsomorphismInspector(graph1, graph2)
    return inspector.isomorphismExists()
}

fun hasIsomorphicStringSG(
    graph1: SimpleGraph<String, DefaultEdge>,
    graph2: SimpleGraph<String, DefaultEdge>
): Boolean {
    val inspector = VF2SubgraphIsomorphismInspector(graph1, graph2)
    return inspector.isomorphismExists()
}

fun hasIsomorphicNodeDDG(
    graph1: DefaultDirectedGraph<Node, DefaultEdge>,
    graph2: DefaultDirectedGraph<Node, DefaultEdge>
): Boolean {
    val inspector = VF2SubgraphIsomorphismInspector(graph1, graph2)
    return inspector.isomorphismExists()
}

fun hasIsomorphicNodeDG(
    graph1: SimpleGraph<Node, DefaultEdge>,
    graph2: SimpleGraph<Node, DefaultEdge>
): Boolean {
    val inspector = VF2SubgraphIsomorphismInspector(graph1, graph2)
    return inspector.isomorphismExists()
}

fun main2() {
    val code1 = CodeSnippets.canonicalBSearch()
    val code2 = CodeSnippets.restructuredBSearch()

    val graph1 = codeToGraph(code1)
    val graph2 = codeToGraph(code2)

    val hasPlagiarism = hasIsomorphicStringDDG(graph1, graph2)

    println("Заимствования найдены: $hasPlagiarism")
}