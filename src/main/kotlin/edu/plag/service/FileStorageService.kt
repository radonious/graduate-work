package edu.plag.service

import edu.plag.exceptions.InvalidFileTypeException
import edu.plag.util.FileUtils
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
        Files.createDirectories(FileUtils.getUploadPath())
    }

    companion object {

    }

    private fun createFileNamePrefix(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss-SSS"))
    }

    fun saveSnippet(code: String): String {
        val fileName = "${createFileNamePrefix()}_snippet.java"
        val uploadsDir = FileUtils.getUploadPath()  // Получаем путь к папке uploads
        val filePath = uploadsDir.resolve(fileName) // Формируем полный путь к файлу
        Files.write(filePath, code.toByteArray())
        return fileName
    }

    fun saveFile(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: throw InvalidFileTypeException("Файл не имеет имени")

        if (!originalFilename.endsWith(".java", ignoreCase = true)) {
            throw InvalidFileTypeException("Допускаются только файлы с расширением .java")
        }

        val fileName = "${createFileNamePrefix()}_${originalFilename}"
        val targetLocation = FileUtils.getUploadPath().resolve(fileName)

        Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
        return fileName
    }

    fun saveArchive(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: throw InvalidFileTypeException("Файл не имеет имени")

        if (!originalFilename.endsWith(".zip", ignoreCase = true)) {
            throw InvalidFileTypeException("Допускаются только .zip архивы")
        }

        val archivePath = FileUtils.getUploadPath().resolve(originalFilename)
        file.transferTo(archivePath.toFile())

        val archiveName = archivePath.fileName.toString().removeSuffix(".zip")
        val prefix = "${createFileNamePrefix()}_"
        val folderName = "${prefix}${archiveName}"
        val extractDir = FileUtils.getUploadPath().resolve(folderName)

        extractDir.createDirectories()
        FileUtils.unzip(archivePath.toFile(), extractDir.toFile())

        // Удаление системных файлов и директорий (снизу вверх)
        extractDir.toFile().walkBottomUp().forEach {
            if (FileUtils.systemEntries.contains(it.name)) {
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