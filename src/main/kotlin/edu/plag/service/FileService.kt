package edu.plag.service

import edu.plag.util.FileUtils
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory

@Service
class FileService {

    fun getAllSavedFiles(): List<Path> {
        return Files.walk(FileUtils.getUploadPath())
            .filter { !it.isDirectory() }
            .toList()
    }
}