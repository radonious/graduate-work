package edu.plag.controller

import edu.plag.dto.*
import edu.plag.service.AuthService
import edu.plag.service.TokenService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val tokenService: TokenService
) {

    @Operation(summary = "Register new user")
    @PostMapping(value = ["/register"], consumes = ["application/json"])
    fun registerUser(@RequestBody registrationRequest: UserRequest): ResponseEntity<String> {
        authService.registerUser(registrationRequest)
        return ResponseEntity.status(201).build()
    }

    @Operation(summary = "Login by username and password")
    @PostMapping(value = ["/login"], consumes = ["application/json"], produces = ["application/json"])
    fun login(@RequestBody authRequest: AuthRequest): ResponseEntity<AuthResponse> {
        val authResponse: AuthResponse = authService.authAndGenerateTokens(authRequest)
        return ResponseEntity.ok(authResponse)
    }

    @Operation(summary = "Refresh old access token")
    @PostMapping(value = ["/refresh-token"], consumes = ["application/json"])
    fun refreshToken(@RequestBody refreshTokenRequest: @Valid RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> {
        val refreshTokenResponse: RefreshTokenResponse =
            tokenService.refreshAccessToken(refreshTokenRequest.refreshToken)
        return ResponseEntity.ok(refreshTokenResponse)
    }

    // TODO: добавить проверку на выход не из своего аккаунта
    @Operation(summary = "Logout user")
    @PostMapping(value = ["/logout"], consumes = ["application/json"])
    fun logoutUser(@RequestBody logoutRequest: LogoutRequest): ResponseEntity<String> {
        tokenService.deleteRefreshToken(logoutRequest.username)
        return ResponseEntity.ok().build()
    }
}