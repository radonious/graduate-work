package edu.plag.dto

import edu.plag.util.FileUtils
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readLines
import kotlin.io.path.readText

data class FileInfo(
    val filename: String,
    val loaded: String,
    val lines: Int,
) {
    companion object {
        fun from(file: Path): FileInfo {
            val content = file.readText()
            val lines = content.lineSequence().count()
            val parts = file.name.split('_')
            val loaded = parts.take(2).joinToString("_")
            val filename = parts.drop(2).joinToString("_")
            return FileInfo(
                filename = filename,
                loaded = loaded,
                lines = lines
            )
        }
    }
}