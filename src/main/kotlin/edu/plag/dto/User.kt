package edu.plag.dto

import edu.plag.enums.UserRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UserRequest(
    @field:NotBlank(message = "Username is mandatory")
    val username: String,
    @field:NotBlank(message = "Password is mandatory")
    val password: String,
    @field:NotNull(message = "Role is mandatory. Roles: USER, CREATOR")
    val role: UserRole,
)

data class UserResponse(
    val id: Long,
    val username: String,
    val role: UserRole,
)
