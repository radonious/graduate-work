package edu.plag.controller

import edu.plag.service.FileStorageService
import edu.plag.util.FileStorageProperties
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import kotlin.io.path.createDirectories

@RestController
@RequestMapping("/api/v1/upload")
class FileUploadController(
    private val fileStorageService: FileStorageService
) {

    @PostMapping("/file")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        val filename = fileStorageService.saveFile(file)
        return ResponseEntity.ok("Файл загружен: $filename")
    }

    @PostMapping("/archive")
    fun uploadArchive(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        val archivePath =  FileStorageProperties.getUploadPath().resolve(file.originalFilename!!)
        file.transferTo(archivePath.toFile()) // Сохранение архива

        val extractDir = FileStorageProperties.getUploadPath().resolve(archivePath.fileName.toString().replace(".zip", ""))
        extractDir.createDirectories() // Создание папки для распаковки

        FileStorageProperties.unzip(archivePath.toFile(), extractDir.toFile()) // Распаковываем архив

        return ResponseEntity.ok("Архив загружен: ${extractDir.fileName}")
    }
}