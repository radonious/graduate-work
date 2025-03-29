package edu.plag.service

import edu.plag.util.FileStorageProperties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.name

@Service
class FileStorageService {
    init {
        Files.createDirectories(FileStorageProperties.getUploadPath())
    }

    private fun createFileNamePrefix(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"))
    }

    fun saveFile(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("Файл не имеет имени")

        if (!originalFilename.endsWith(".java", ignoreCase = true)) {
            throw IllegalArgumentException("Допускаются только файлы с расширением .java")
        }

        val fileName = "${createFileNamePrefix()}_${originalFilename}"
        val targetLocation = FileStorageProperties.getUploadPath().resolve(fileName)

        Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
        return fileName
    }

    fun saveArchive(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("Файл не имеет имени")

        val archivePath = FileStorageProperties.getUploadPath().resolve(originalFilename)
        file.transferTo(archivePath.toFile()) // Сохранение архива

        val archiveName = archivePath.fileName.toString().replace(".zip", "")
        val folderName = "${createFileNamePrefix()}_${archiveName}"
        val extractDir = FileStorageProperties.getUploadPath().resolve(folderName)

        extractDir.createDirectories() // Создание папки для распаковки

        FileStorageProperties.unzip(archivePath.toFile(), extractDir.toFile()) // Распаковываем архив

        var countJavaFiles = 0
        extractDir.toFile().walk().forEach {
            if (it.isFile && !it.extension.equals("java", ignoreCase = true)) {
                it.delete() // Удаляем файлы, которые не .java
                ++countJavaFiles
            }
        }

        Files.deleteIfExists(archivePath) // Удаление архива

        if (countJavaFiles == 0) {
            Files.deleteIfExists(extractDir)
            throw IllegalArgumentException("В проекте не содержится ни одного файла .java")
        }

        return extractDir.name
    }
}