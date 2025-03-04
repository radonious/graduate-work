package edu.plag

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import soot.G
import soot.Scene
import soot.jimple.toolkits.callgraph.CHATransformer
import soot.jimple.toolkits.callgraph.CallGraph
import soot.options.Options
import java.io.File

fun main() {
    val file1 = getResourceFile("Code1.java") ?: error("Файл Code1.java не найден!")
    val file2 = getResourceFile("Code2.java") ?: error("Файл Code2.java не найден!")


    val ast1 = parseJavaFile(file1)
    val ast2 = parseJavaFile(file2)

    if (ast1 != null && ast2 != null) {
        val normalized1 = normalizeAST(ast1)
        val normalized2 = normalizeAST(ast2)

//        val antlrStructure1 = parseWithANTLR(file1)
//        val antlrStructure2 = parseWithANTLR(file2)

        val sootGraph1 = analyzeWithSoot(file1)
        val sootGraph2 = analyzeWithSoot(file2)

        val similarityAST = compareAST(normalized1, normalized2)
//        val similarityANTLR = if (antlrStructure1 == antlrStructure2) 1.0 else 0.0
        val similaritySoot = if (sootGraph1 == sootGraph2) 1.0 else 0.0

        val finalScore = (similarityAST * 0.7 + similaritySoot * 0.3) * 100
        println("Общая схожесть кода: ${"%.2f".format(finalScore)}%")
    } else {
        println("Ошибка при парсинге файлов")
    }
}

fun getResourceFile(filename: String): File? {
    val resource = object {}.javaClass.getResource("/$filename")
    return resource?.toURI()?.let { File(it) }
}

/** Парсит Java-файл с помощью JavaParser */
fun parseJavaFile(file: File): CompilationUnit? {
    return try {
        StaticJavaParser.parse(file)
    } catch (e: Exception) {
        println("Ошибка парсинга ${file.name}: ${e.message}")
        null
    }
}

/** Нормализует AST: заменяет имена переменных, методов и классов */
fun normalizeAST(ast: CompilationUnit): CompilationUnit {
    val variableMap = mutableMapOf<String, String>()
    val methodMap = mutableMapOf<String, String>()
    var varCounter = 1
    var funcCounter = 1

    ast.walk { node ->
        when (node) {
            is VariableDeclarator -> {
                val name = node.nameAsString
                if (!variableMap.containsKey(name)) {
                    variableMap[name] = "VAR$varCounter"
                    varCounter++
                }
                node.setName(variableMap[name])
            }

            is MethodDeclaration -> {
                val name = node.nameAsString
                if (!methodMap.containsKey(name)) {
                    methodMap[name] = "FUNC$funcCounter"
                    funcCounter++
                }
                node.setName(methodMap[name])
            }
        }
    }
    return ast
}

/** Сравнивает AST с учетом структуры */
fun compareAST(ast1: CompilationUnit, ast2: CompilationUnit): Double {
    return if (ast1.toString() == ast2.toString()) 1.0 else 0.0
}

/** Использует ANTLR для разбора структуры кода */
//fun parseWithANTLR(file: File): String {
//    val inputStream = CharStreams.fromFileName(file.absolutePath)
//    val lexer = JavaLexer(inputStream)
//    val tokens = CommonTokenStream(lexer)
//    val parser = JavaParser(tokens)
//    return parser.compilationUnit().toStringTree(parser)
//}

/** Анализирует код с помощью Soot и строит граф вызовов */
fun analyzeWithSoot(file: File): CallGraph {
    G.reset()
    Options.v().set_prepend_classpath(true)
    Options.v().set_process_dir(listOf(file.parent))
    Options.v().set_soot_classpath(file.absolutePath)
    Options.v().set_whole_program(true)
    Options.v().set_allow_phantom_refs(true)

    Scene.v().loadNecessaryClasses()
    CHATransformer.v().transform()

    return Scene.v().callGraph
}
