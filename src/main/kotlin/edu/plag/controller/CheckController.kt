package edu.plag.controller

import edu.plag.dto.CheckResults
import edu.plag.dto.CheckSettings
import edu.plag.service.CheckService
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
    private val fileStorageService: FileStorageService,
    private val checkService: CheckService,
) {
    @Operation(summary = "Check code snippet for plagiarism")
    @PostMapping(value = ["/snippet"], consumes = ["multipart/form-data"])
    fun checkCodeSnippet(
        @RequestPart("snippet") @Valid snippet: String,
        @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<CheckResults> {
        val res = checkService.checkSnippet(snippet, settings)
        // TODO: (LATER) добавить cron для очистки одинаковых файлов в базе (или проверять/блокировать их добавление)
        if (settings.saveFileInDatabase) fileStorageService.saveSnippet(snippet)
        return ResponseEntity.ok(res)
    }

    @Operation(summary = "Check file for plagiarism")
    @PostMapping(value = ["/file"], consumes = ["multipart/form-data"])
    fun checkFile(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<String> {

        if (settings.saveFileInDatabase) fileStorageService.saveFile(file)
        return ResponseEntity.ok("Проверка файла выполнена\n${file.name}\n$settings")
    }

    @Operation(summary = "Check project for plagiarism")
    @PostMapping(value = ["/archive"], consumes = ["multipart/form-data"])
    fun checkArchive(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<String> {
        // Вернуть сам проект
        // Начать проверку
        if (settings.saveFileInDatabase) fileStorageService.saveArchive(file)
        return ResponseEntity.ok("Проверка проекта выполнена\n${file.name}\n$settings")
    }
}
