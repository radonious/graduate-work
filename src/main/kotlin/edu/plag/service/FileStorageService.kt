package edu.plag.service

import edu.plag.entity.File
import edu.plag.exceptions.InvalidFileTypeException
import edu.plag.repository.FileRepository
import edu.plag.util.FileUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories

@Service
class FileStorageService(
    private val fileRepository: FileRepository
) {

    init {
        Files.createDirectories(FileUtils.getUploadPath())
    }

    @Transactional
    fun saveSnippet(code: String): Unit {
        val hash = computeHash(code)
        if (fileRepository.findById(hash).isPresent) return
        val fileName = "${createFileNamePrefix()}_snippet.java"
        val uploadsDir = FileUtils.getUploadPath()  // Получаем путь к папке uploads
        val filePath = uploadsDir.resolve(fileName) // Формируем полный путь к файлу
        Files.write(filePath, code.toByteArray())
        fileRepository.save(File(hash = hash, name = "snippet.java"))
    }

    @Transactional
    fun saveFile(file: MultipartFile): Unit {
        // Проверки
        val originalFilename = file.originalFilename ?: throw InvalidFileTypeException("File name is invalid")

        if (!originalFilename.endsWith(".java", ignoreCase = true)) {
            throw InvalidFileTypeException("Only .java files are allowed")
        }

        val hash = computeHashInputStream(file.inputStream)
        if (fileRepository.findById(hash).isPresent) return

        // Сохранение
        val fileName = "${createFileNamePrefix()}_${originalFilename}"
        val targetLocation = FileUtils.getUploadPath().resolve(fileName)

        fileRepository.save(File(hash = hash, name = originalFilename))
        Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
    }

    @Transactional
    fun saveArchive(file: MultipartFile): Unit {
        val originalFilename = file.originalFilename ?: throw InvalidFileTypeException("File name is invalid")

        if (!originalFilename.endsWith(".zip", ignoreCase = true)) {
            throw InvalidFileTypeException("Only .zip archives are allowed")
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
        val savedFiles = mutableListOf<File>()
        extractDir.toFile().walk().forEach {
            if (it.isFile) {
                if (!it.extension.equals("java", ignoreCase = true)) {
                    it.delete()
                } else {
                    val hash = computeHashInputStream(it.inputStream())
                    if (fileRepository.findById(hash).isPresent) {
                        it.delete()
                        return@forEach
                    }
                    // Переименовываем файл, сохраняя структуру папок
                    val newName = prefix + it.name
                    val newPath = it.toPath().resolveSibling(newName)
                    savedFiles.add(File(hash, it.name))
                    Files.move(it.toPath(), newPath)
                    countJavaFiles++
                }
            }
        }

        fileRepository.saveAll(savedFiles)

        // Удаляем пустые директории (снизу вверх)
        extractDir.toFile().walkBottomUp().forEach {
            if (it.isDirectory && it.listFiles().isNullOrEmpty()) {
                it.delete()
            }
        }

        Files.deleteIfExists(archivePath)
    }

    private fun createFileNamePrefix(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss-SSS"))
    }

    private fun computeHash(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = content.toByteArray(Charsets.UTF_8)
        digest.update(bytes)
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun computeHashInputStream(stream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        stream.use { input ->
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
