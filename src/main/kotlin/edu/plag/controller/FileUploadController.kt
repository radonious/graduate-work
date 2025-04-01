package edu.plag.controller

import edu.plag.service.FileStorageService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/upload")
@Validated
class FileUploadController(
    private val fileStorageService: FileStorageService
) {

    @PostMapping("/file")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        val filename = fileStorageService.saveFile(file)
        return ResponseEntity.ok("File successfully loaded: $filename")
    }

    @PostMapping("/archive")
    fun uploadArchive(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        val folderName = fileStorageService.saveArchive(file)
        return ResponseEntity.ok("Archive successfully loaded: $folderName")
    }
}
