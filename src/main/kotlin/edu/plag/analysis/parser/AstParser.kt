package edu.plag.analysis.parser

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node


/**
 * Парсит Java-код и строит AST (абстрактное синтаксическое дерево)
 */
fun parseAst(code: String): CompilationUnit {
    return StaticJavaParser.parse(code)
}

/**
 * Рекурсивный обход AST для извлечения синтаксических данных
 */
fun traverseAst(node: Node, level: Int = 0) {
    println("${"  ".repeat(level)}${node.javaClass.simpleName} ${if (node.childNodes.isEmpty()) ": " + node.tokenRange.get() else ""}")
    node.childNodes.forEach { traverseAst(it, level + 1) }
}
