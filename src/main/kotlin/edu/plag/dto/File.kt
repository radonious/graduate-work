package edu.plag.dto

import edu.plag.exceptions.InvalidFileTypeException
import edu.plag.util.FileUtils.Companion.createFileNamePrefix
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.text.Charsets.UTF_8

data class FileInfo(
    val filename: String,
    val prefix: String?,
    val lines: Int,
) {
    companion object {
        fun fromPath(file: Path): FileInfo {
            val content = file.readText()
            val lines = content.lineSequence().count()
            val prefix = file.name.substringBeforeLast('_')
            val filename = file.name.substringAfterLast('_')
            return FileInfo(
                filename = filename,
                prefix = prefix,
                lines = lines
            )
        }

        fun fromSnippet(code: String): FileInfo {
            val lines = code.lines().size
            return FileInfo(
                filename = "snippet",
                prefix = createFileNamePrefix(),
                lines = lines,
            )
        }

        fun fromMultipartFile(file: MultipartFile): FileInfo {
            val filename = file.originalFilename ?: throw InvalidFileTypeException("Can not identify filename")
            val content = String(file.bytes, UTF_8)
            val lines = content.lines().size
            return FileInfo(
                filename = filename,
                prefix = createFileNamePrefix(),
                lines = lines,
            )
        }
    }
}
