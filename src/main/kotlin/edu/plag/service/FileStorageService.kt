package edu.plag.service

import edu.plag.entity.File
import edu.plag.exceptions.InvalidFileTypeException
import edu.plag.repository.FileRepository
import edu.plag.util.FileUtils
import edu.plag.util.FileUtils.Companion.systemEntries
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.io.path.createDirectories

@Service
class FileStorageService(
    private val fileRepository: FileRepository
) {

    init {
        Files.createDirectories(FileUtils.getUploadPath())
    }

    @Transactional
    fun saveSnippet(code: String) {
        val hash = computeHash(code)
        if (fileRepository.findById(hash).isPresent) return
        val fileName = "${createFileNamePrefix()}_snippet.java"
        val uploadsDir = FileUtils.getUploadPath()  // Получаем путь к папке uploads
        val filePath = uploadsDir.resolve(fileName) // Формируем полный путь к файлу
        Files.write(filePath, code.toByteArray())
        fileRepository.save(File(hash = hash, name = "snippet.java"))
    }

    @Transactional
    fun saveFile(file: MultipartFile) {
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
    fun saveArchive(file: MultipartFile) {
        val originalFilename = file.originalFilename ?: throw InvalidFileTypeException("File name is invalid")

        if (!originalFilename.endsWith(".zip", ignoreCase = true)) {
            throw InvalidFileTypeException("Only .zip archives are allowed")
        }

        val archiveFile = FileUtils.getUploadPath().resolve(originalFilename)
        file.transferTo(archiveFile.toFile())

        val archiveName = archiveFile.fileName.toString().removeSuffix(".zip")
        val prefix = "${createFileNamePrefix()}_"
        val folderName = "${prefix}${archiveName}"
        val extractDir = FileUtils.getUploadPath().resolve(folderName)

        extractDir.createDirectories()

        val createdDirectories = mutableSetOf<java.io.File>(extractDir.toFile())
        val savedFiles = mutableSetOf<File>()

        ZipInputStream(FileInputStream(archiveFile.toFile())).use { zis ->
            generateSequence { zis.nextEntry }.forEach { entry ->
                if (isValidEntry(entry)) {
                    val outputFile = File(extractDir.toFile(), entry.name)

                    if (entry.isDirectory) {
                        if (outputFile.mkdirs()) createdDirectories.add(outputFile)
                    } else {
                        outputFile.parentFile?.takeUnless { it.exists() }?.let {
                            it.mkdirs()
                            createdDirectories.add(it)
                        }

                        // Чтение байтов ZipEntry
                        ByteArrayOutputStream().use { baos ->
                            zis.copyTo(baos)
                            val bytes = baos.toByteArray()
                            ByteArrayInputStream(bytes).use { inputStream ->
                                // Проверка хеша (до сохранения, чтобы не удалять)
                                val hash = computeHashInputStream(inputStream)
                                if (fileRepository.findById(hash).isPresent) return@forEach
                                savedFiles.add(File(hash = hash, name = entry.name.substringAfterLast("/")))
                            }

                            // Запись в файл
                            FileOutputStream(outputFile).use { fos ->
                                fos.write(bytes)
                            }
                        }

                        trackParentDirectories(outputFile.parentFile, extractDir.toFile(), createdDirectories)
                    }
                }
            }
        }

        // Сохраняем хеши
        fileRepository.saveAll(savedFiles)

        // Удаляем пустые директории (начиная с самых вложенных)
        createdDirectories
            .sortedByDescending { it.path.length }
            .forEach { dir ->
                if (dir.isDirectory && dir.listFiles()?.isEmpty() == true) {
                    dir.deleteRecursively()
                }
            }

        Files.deleteIfExists(archiveFile)
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

    private fun isValidEntry(entry: ZipEntry): Boolean {
        // Проверка на системные директории
        val pathSegments = entry.name.split('/')
        if (pathSegments.any { it in systemEntries }) return false

        // Проверка на .java файл
        return when {
            entry.isDirectory -> true // Временно сохраняем директории
            entry.name.endsWith(".java", ignoreCase = true) -> true
            else -> false
        }

    }

    private fun trackParentDirectories(
        currentDir: java.io.File?,
        rootDir: java.io.File,
        directories: MutableSet<java.io.File>
    ) {
        var dir = currentDir
        while (dir != null && dir != rootDir) {
            directories.add(dir)
            dir = dir.parentFile
        }
    }
}
