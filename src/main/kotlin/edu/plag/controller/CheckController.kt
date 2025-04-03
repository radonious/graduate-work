package edu.plag.controller

import edu.plag.dto.CheckResults
import edu.plag.dto.CheckSettings
import edu.plag.exceptions.InvalidFileTypeException
import edu.plag.service.CheckService
import edu.plag.service.FileStorageService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

// TODO: (LATER) добавить cron для очистки одинаковых файлов в базе (или проверять/блокировать их добавление)
//  как вариант - проверять количество схожих файлов, если их более 50%, то удалять старый проект и оставлять новый (проблема с поиском проекта в базе, хотя имена должны быстро сравниваться)
//  для ускорения поиска можно в папку с проектом добавлять .txt с перечислением всех его файлов, так значительно быстрее
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
        @RequestPart("snippet") @Valid snippet: String, @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<CheckResults> = runBlocking {
        val res = checkService.checkSnippet(snippet, settings)
        if (settings.saveFileInDatabase) fileStorageService.saveSnippet(snippet)
        return@runBlocking ResponseEntity.ok(res)
    }

    @Operation(summary = "Check file for plagiarism")
    @PostMapping(value = ["/file"], consumes = ["multipart/form-data"])
    fun checkFile(
        @RequestPart("file") file: MultipartFile, @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<CheckResults> = runBlocking {
        val res = checkService.checkFile(file, settings)
        if (settings.saveFileInDatabase) fileStorageService.saveFile(file)
        return@runBlocking ResponseEntity.ok(res)
    }

    @Operation(summary = "Check project for plagiarism")
    @PostMapping(value = ["/archive"], consumes = ["multipart/form-data"])
    fun checkArchive(
        @RequestPart("file") file: MultipartFile, @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<CheckResults> = runBlocking {
        val originalFilename = file.originalFilename ?: throw InvalidFileTypeException("Файл не имеет имени")
        if (!originalFilename.endsWith(".zip", ignoreCase = true)) {
            throw InvalidFileTypeException("Допускаются только .zip архивы")
        }
        val res = checkService.checkArchive(file, settings)
        if (settings.saveFileInDatabase) fileStorageService.saveArchive(file)
        return@runBlocking ResponseEntity.ok(res)
    }
}
