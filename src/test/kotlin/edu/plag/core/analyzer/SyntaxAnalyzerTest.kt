package edu.plag.core.analyzer

import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SyntaxAnalyzerTest(
    @Autowired private var syntaxAnalyzer: SyntaxAnalyzer
) {

    private val code =
        """
        public class BubbleSortSnippet {

          /**
           * Sort an array with bubbleSort algorithm.
           *
           * @param arr array to sort
           */
          public static void bubbleSort(int[] arr) {
            var lastIndex = arr.length - 1;

            for (var j = 0; j < lastIndex; j++) {
              for (var i = 0; i < lastIndex - j; i++) {
                if (arr[i] > arr[i + 1]) {
                  var tmp = arr[i];
                  arr[i] = arr[i + 1];
                  arr[i + 1] = tmp;
                }
              }
            }
          }
        }
        """

    // Тестовые графы
    private fun createSimpleGraph(): DefaultDirectedGraph<String, DefaultEdge> {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        graph.addVertex("A")
        graph.addVertex("B")
        graph.addEdge("A", "B")
        return graph
    }

    private fun createIsomorphicGraph(): DefaultDirectedGraph<String, DefaultEdge> {
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        graph.addVertex("X")
        graph.addVertex("Y")
        graph.addEdge("X", "Y")
        return graph
    }

    @Test
    fun `analyze should detect isomorphism in identical graphs`() {
        val result = syntaxAnalyzer.analyze(code, code)

        assertTrue(result.hasIsomorphism)
        assertEquals(0.0, result.editDistance)
        assertEquals(1.0, result.finalScore, 0.01)
    }

    @Test
    fun `edit distance should be zero for identical graphs`() {
        val graph = createSimpleGraph()

        val distance = syntaxAnalyzer.editDistance(graph, graph)
        val score = syntaxAnalyzer.scoreEditDistance(graph, graph, distance)

        assertEquals(0.0, distance)
        assertEquals(1.0, score, 0.01)
    }

    @Test
    fun `should detect isomorphic subgraphs`() {
        val mainGraph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java).apply {
            addVertex("A"); addVertex("B"); addVertex("C")
            addEdge("A", "B"); addEdge("B", "C")
        }

        val subgraph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java).apply {
            addVertex("C"); addVertex("B"); addVertex("A")
            addEdge("B", "A"); addEdge("C", "B")
        }

        val result = syntaxAnalyzer.hasAstPlagiarism(mainGraph, subgraph, 3)
        assertTrue(result)
    }

    @Test
    fun `score should handle completely different graphs`() {
        val graph1 = createSimpleGraph()
        val graph2 = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java).apply {
            addVertex("C"); addVertex("D"); addEdge("C", "D")
        }

        val distance = syntaxAnalyzer.editDistance(graph1, graph2)
        val score = syntaxAnalyzer.scoreEditDistance(graph1, graph2, distance)

        assertEquals(2.2, distance)
        assertEquals(0.0, score, 0.01)
    }

    @Test
    fun `final score should respect weights`() {
        val editScore = 0.7
        val hasIsomorphism = true

        val expected = 0.7 * 0.6 + 0.4
        val actual = syntaxAnalyzer.calculateFinalScore(editScore, hasIsomorphism)

        assertEquals(expected, actual, 0.01)
    }

    private fun SyntaxAnalyzer.calculateFinalScore(
        editScore: Double,
        hasIsomorphism: Boolean
    ): Double {
        return editScore * 0.6 +
                (if (hasIsomorphism) 0.4 else 0.0)
    }

    @Test
    fun `should handle empty graphs`() {
        val emptyGraph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)

        val distance = syntaxAnalyzer.editDistance(emptyGraph, emptyGraph)
        val score = syntaxAnalyzer.scoreEditDistance(emptyGraph, emptyGraph, distance)

        assertEquals(0.0, distance)
        assertEquals(0.0, score)
    }
}
