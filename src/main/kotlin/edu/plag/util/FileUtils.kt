package edu.plag.util

import org.springframework.stereotype.Component
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipFile

@Component
class FileUtils {

    companion object {
        fun getUploadPath(): Path = Paths.get("uploads").toAbsolutePath().normalize().also {
            Files.createDirectories(it)
        }

        fun unzip(zipFile: File, destDir: File) {
            ZipFile(zipFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val file = File(destDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }
        }

        fun tryReadFile(file: Path, maxAttempts: Int = 3): String {
            val encodings = listOf(
                StandardCharsets.UTF_8,
                Charset.forName("Windows-1251"),
                StandardCharsets.ISO_8859_1
            )

            var lastException: Exception? = null

            for (encoding in encodings.take(maxAttempts)) {
                try {
                    return Files.readString(file, encoding)
                } catch (e: Exception) {
                    lastException = e
                }
            }

            throw lastException ?: IllegalStateException("Не удалось прочитать файл: $file")
        }
    }
}