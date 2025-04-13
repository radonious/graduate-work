package edu.plag.controller

import edu.plag.dto.UserRequest
import edu.plag.dto.UserResponse
import edu.plag.service.UserService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/users")
@Validated
class UserController(
    private val userService: UserService,
) {
    @Operation(summary = "Find all existing users")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users: List<UserResponse> = userService.findAllUsers()
        return ResponseEntity.ok(users)
    }

    @Operation(summary = "Find user by specific ID")
    @PreAuthorize("hasRole('ADMIN') or @authService.isAuthenticatedUserWithId(#id)")
    @GetMapping(value = ["/{id}"], produces = ["application/json"])
    fun getUserById(@PathVariable id: Int): ResponseEntity<UserResponse> {
        val user: UserResponse = userService.findUserById(id.toLong())
        return ResponseEntity.ok(user)
    }

    @Operation(summary = "Create new user")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = ["application/json"], produces = ["application/json"])
    fun createUser(@RequestBody userRequestDto: @Valid UserRequest): ResponseEntity<UserResponse> {
        val createdUser: UserResponse = userService.createUser(userRequestDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @Operation(summary = "Update user information")
    @PreAuthorize("hasRole('ADMIN') or @authService.isAuthenticatedUserWithId(#id)")
    @PutMapping(value = ["/{id}"], consumes = ["application/json"], produces = ["application/json"])
    fun updateUser(
        @PathVariable id: Int, @RequestBody userRequestDto: @Valid UserRequest
    ): ResponseEntity<UserResponse> {
        val updatedUser: UserResponse = userService.updateUser(id.toLong(), userRequestDto)
        return ResponseEntity.ok(updatedUser)
    }

    @Operation(summary = "Delete user by specific ID")
    @PreAuthorize("hasRole('ADMIN') or @authService.isAuthenticatedUserWithId(#id)")
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Int): ResponseEntity<Void> {
        userService.deleteUser(id.toLong())
        return ResponseEntity.noContent().build()
    }
}
