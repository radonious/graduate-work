package edu.plag.dto

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "refreshToken is mandatory")
    val refreshToken: String,
)

data class RefreshTokenResponse(
    val accessToken: String,
)
