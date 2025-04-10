package edu.plag.service

import edu.plag.dto.AuthRequest
import edu.plag.dto.AuthResponse
import edu.plag.dto.UserRequest
import edu.plag.dto.UserResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service


@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val tokenService: TokenService,
    private val userService: UserService,
    private val jwtService: JwtService,
) {
    /**
     * Аутентифицирует пользователя и возвращает его токен доступа и обновления
     * @param authRequest запрос на логин
     * @return [AuthResponse] ответ с токенами
     */
    fun authAndGenerateTokens(authRequest: AuthRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authRequest.username,
                authRequest.password
            )
        )

        val userDetails = userDetailsService.loadUserByUsername(authRequest.username)
        val accessToken = jwtService.generateToken(userDetails)
        val refreshToken = tokenService.createRefreshToken(authRequest.username)

        tokenService.saveRefreshToken(authRequest.username, refreshToken)

        val user: UserResponse = userService.findUserByUsername(authRequest.username)

        return AuthResponse(
            userId = user.id,
            refreshToken = refreshToken,
            accessToken = accessToken,
            username = user.username,
            role = user.role
        )
    }

    /**
     * Определяет, вошел ли сейчас пользователя
     * @param userId ID пользователя
     * @return true - вошел, false - не вошел
     */
    fun isAuthenticatedUserWithId(userId: Long): Boolean {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        if (authentication.isAuthenticated) {
            val userResponseDto: UserResponse = userService.findUserById(userId)
            return authentication.name == userResponseDto.username
        }
        return false
    }

    /**
     * Создает нового пользователя в базе
     * @param registrationRequest запрос создания
     */
    fun registerUser(registrationRequest: UserRequest) {
        userService.createUser(registrationRequest)
    }

    fun getAuthenticatedUserId(): Long {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        return userService.findUserByUsername(authentication.name).id
    }
}