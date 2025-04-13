package edu.plag.core.parser

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.*
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.springframework.stereotype.Component

@Component
class GraphParser {
    /**
     * Конвертирует AST в граф
     */
    fun parseGraph(unit: CompilationUnit): DefaultDirectedGraph<String, DefaultEdge> {
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

            /**
             * Метод visit() для разных типов Node имеет разную реализацию,
             * авторы JavaParser не сделали универсальный visit().
             */
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

}
