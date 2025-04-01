package edu.plag.dto

import edu.plag.enums.UserRole
import jakarta.validation.constraints.NotBlank


data class AuthRequest(
    @field:NotBlank(message = "Username is mandatory")
    val username: String,
    @field:NotBlank(message = "Password is mandatory")
    val password: String,
)

data class AuthResponse(
    val userId: Long,
    val username: String,
    val role: UserRole,
    val accessToken: String,
    val refreshToken: String,
)

data class LogoutRequest(
    @NotBlank(message = "Username is mandatory")
    val username: String,
)
