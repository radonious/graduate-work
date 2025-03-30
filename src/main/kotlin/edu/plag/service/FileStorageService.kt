package edu.plag.service

import edu.plag.exceptions.InvalidFileTypeException
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

    // TODO: нужно ли сохранять файлы (путь, автор и тп) в бд, если по сути они просто перебираются
    //  вар 1. Нет, при проверке мы перебираем все/какие-то файлы
    //  вар 2. Да, например для информации об истории чего-то

    init {
        Files.createDirectories(FileStorageProperties.getUploadPath())
    }

    companion object {
        private val systemEntries: Set<String> = setOf(
            // Список системных файлов
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
            // А также директории для удаления
            "generated",
            "test"
        )
    }

    private fun createFileNamePrefix(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"))
    }

    fun saveFile(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: throw InvalidFileTypeException("Файл не имеет имени")

        if (!originalFilename.endsWith(".java", ignoreCase = true)) {
            throw InvalidFileTypeException("Допускаются только файлы с расширением .java")
        }

        val fileName = "${createFileNamePrefix()}_${originalFilename}"
        val targetLocation = FileStorageProperties.getUploadPath().resolve(fileName)

        Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
        return fileName
    }

    fun saveArchive(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: throw InvalidFileTypeException("Файл не имеет имени")

        val archivePath = FileStorageProperties.getUploadPath().resolve(originalFilename)
        file.transferTo(archivePath.toFile())

        val archiveName = archivePath.fileName.toString().removeSuffix(".zip")
        val prefix = "${createFileNamePrefix()}_"
        val folderName = "${prefix}${archiveName}"
        val extractDir = FileStorageProperties.getUploadPath().resolve(folderName)

        extractDir.createDirectories()
        FileStorageProperties.unzip(archivePath.toFile(), extractDir.toFile())

        // Удаление системных файлов и директорий (снизу вверх)
        extractDir.toFile().walkBottomUp().forEach {
            if (systemEntries.contains(it.name)) {
                if (it.isDirectory) {
                    it.deleteRecursively()
                } else {
                    it.delete()
                }
            }
        }

        var countJavaFiles = 0
        extractDir.toFile().walk().forEach {
            if (it.isFile) {
                if (!it.extension.equals("java", ignoreCase = true)) {
                    it.delete()
                } else {
                    // Переименовываем файл, сохраняя структуру папок
                    val newName = prefix + it.name
                    val newPath = it.toPath().resolveSibling(newName)
                    Files.move(it.toPath(), newPath)
                    countJavaFiles++
                }
            }
        }

        // Удаляем пустые директории (снизу вверх)
        extractDir.toFile().walkBottomUp().forEach {
            if (it.isDirectory && it.listFiles().isNullOrEmpty()) {
                it.delete()
            }
        }

        Files.deleteIfExists(archivePath)

        if (countJavaFiles == 0) {
            Files.deleteIfExists(extractDir)
            throw InvalidFileTypeException("В проекте не содержится ни одного файла .java")
        }

        return extractDir.name
    }
}