package edu.plag.dto

data class RefreshTokenRequest(
    val refreshToken: String,
)

data class RefreshTokenResponse(
    val accessToken: String,
)
