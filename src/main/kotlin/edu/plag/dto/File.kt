package edu.plag.dto

import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readText

data class FileInfo(
    val filename: String,
    val prefix: String,
    val lines: Int,
) {
    companion object {
        fun from(file: Path): FileInfo {
            val content = file.readText()
            val lines = content.lineSequence().count()
            val parts = file.name.split('_')
            val prefix = parts.take(2).joinToString("_")
            val filename = parts.drop(2).joinToString("_")
            return FileInfo(
                filename = filename,
                prefix = prefix,
                lines = lines
            )
        }
    }
}