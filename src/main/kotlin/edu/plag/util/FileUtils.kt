package edu.plag.util

import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipFile
import kotlin.io.path.isDirectory

@Component
class FileUtils {

    companion object {
        val systemEntries: Set<String> = setOf(
            ".DS_Store",
            "__MACOSX",
            "Thumbs.db",
            ".Spotlight-V100",
            ".Trashes",
            "desktop.ini",
            ".AppleDouble",
            "ehthumbs_vista.db",
            ".TemporaryItems",
            ".apdisk",
            ".fseventsd",
            ".VolumeIcon.icns",
            "generated",
            "generated-src",
            "test",
            "build",
            "uploads",
        )

        fun getUploadPath(): Path = Paths.get("uploads").toAbsolutePath().normalize().also {
            Files.createDirectories(it)
        }

        fun getAllSavedFiles(): List<Path> {
            return Files.walk(getUploadPath())
                .filter { !it.isDirectory() }
                .toList()
        }
    }
}