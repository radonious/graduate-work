package edu.plag.controller

import edu.plag.dto.CheckSettings
import edu.plag.service.FileStorageService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/check")
@Validated
class CheckController(
    private val fileStorageService: FileStorageService
) {
    @Operation(summary = "Check code snippet for plagiarism")
    @PostMapping(value = ["/snippet"], consumes = ["multipart/form-data"])
    fun checkCodeSnippet(
        @RequestPart("snippet") @Valid snippet: String,
        @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<String> {
        // ...
        return ResponseEntity.ok("Проверка кода выполнена\n$snippet\n$settings")
    }

    @Operation(summary = "Check file for plagiarism")
    @PostMapping(value = ["/file"], consumes = ["multipart/form-data"])
    fun checkFile(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<String> {
        fileStorageService.saveFile(file)
        // Вернуть сам файл
        // Начать проверку
        return ResponseEntity.ok("Проверка файла выполнена\n${file.name}\n$settings")
    }

    @Operation(summary = "Check project for plagiarism")
    @PostMapping(value = ["/archive"], consumes = ["multipart/form-data"])
    fun checkArchive(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<String> {
        fileStorageService.saveArchive(file)
        // Вернуть сам проект
        // Начать проверку
        return ResponseEntity.ok("Проверка проекта выполнена\n${file.name}\n$settings")
    }
}
