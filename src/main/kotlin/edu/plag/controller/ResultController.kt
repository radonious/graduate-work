package edu.plag.controller

import edu.plag.entity.Result
import edu.plag.service.AuthService
import edu.plag.service.ResultService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/results")
@Validated
class ResultController(
    private val authService: AuthService,
    private val resultService: ResultService
) {

    @Operation(summary = "Get user check results history")
    @GetMapping(produces = ["application/json"])
    fun getUserResults(): ResponseEntity<List<Result>> {
        val userId = authService.getAuthenticatedUserId()
        val results = resultService.getResults(userId)
        return ResponseEntity.ok(results)
    }
}