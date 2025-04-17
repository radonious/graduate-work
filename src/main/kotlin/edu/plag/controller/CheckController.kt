package edu.plag.controller

import edu.plag.dto.CheckResults
import edu.plag.dto.CheckSettings
import edu.plag.enums.CheckType
import edu.plag.exceptions.InvalidFileTypeException
import edu.plag.service.AuthService
import edu.plag.service.CheckService
import edu.plag.service.FileStorageService
import edu.plag.service.ResultService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
    private val authService: AuthService,
    private val resultService: ResultService
) {

    @Operation(summary = "Check code snippet for plagiarism")
    @PostMapping(value = ["/snippet"], consumes = ["multipart/form-data"])
    fun checkCodeSnippet(
        @RequestPart("snippet") @Valid snippet: String,
        @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<CheckResults> = runBlocking {

        val res = checkService.checkSnippet(snippet, settings)
        val userId = authService.getAuthenticatedUserId()
        resultService.saveResults(userId, res, CheckType.SNIPPET)
        if (settings.saveSourcesIntoDatabase) withContext(Dispatchers.IO) { fileStorageService.saveSnippet(snippet) }
        return@runBlocking ResponseEntity.ok(res)
    }

    @Operation(summary = "Check file for plagiarism")
    @PostMapping(value = ["/file"], consumes = ["multipart/form-data"])
    fun checkFile(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<CheckResults> = runBlocking {

        val res = checkService.checkFile(file, settings)
        val userId = authService.getAuthenticatedUserId()
        resultService.saveResults(userId, res, CheckType.FILE)
        if (settings.saveSourcesIntoDatabase) withContext(Dispatchers.IO) { fileStorageService.saveFile(file) }
        return@runBlocking ResponseEntity.ok(res)
    }

    @Operation(summary = "Check project for plagiarism")
    @PostMapping(value = ["/archive"], consumes = ["multipart/form-data"])
    fun checkArchive(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("settings") @Valid settings: CheckSettings
    ): ResponseEntity<CheckResults> = runBlocking {

        val originalFilename = file.originalFilename ?: throw InvalidFileTypeException("File name is invalid")
        if (!originalFilename.endsWith(".zip", ignoreCase = true)) {
            throw InvalidFileTypeException("Only .zip archives are allowed")
        }

        val res = checkService.checkArchive(file, settings)
        val userId = authService.getAuthenticatedUserId()
        resultService.saveResults(userId, res, CheckType.ARCHIVE)
        if (settings.saveSourcesIntoDatabase) withContext(Dispatchers.IO) { fileStorageService.saveArchive(file) }
        return@runBlocking ResponseEntity.ok(res)
    }
}
