package edu.plag.service

import edu.plag.util.FileStorageProperties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class FileStorageService {
    init {
        Files.createDirectories(FileStorageProperties.getUploadPath())
    }

    fun saveFile(file: MultipartFile): String {
        val filename = "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"))}_${file.originalFilename}"
        val targetLocation = FileStorageProperties.getUploadPath().resolve(filename)

        Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
        return filename
    }
}