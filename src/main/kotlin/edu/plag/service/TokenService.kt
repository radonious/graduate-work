package edu.plag.service

import edu.plag.dto.RefreshTokenResponse
import edu.plag.entity.User
import edu.plag.exceptions.RefreshTokenException
import edu.plag.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class TokenService(
    val jwtService: JwtService,
    val userRepository: UserRepository,
    val userDetailsService: UserDetailsService
) {

    /**
     * Создает токен обновления
     * @param [username] имя пользователя
     * @return новый токен
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun createRefreshToken(username: String): String {
        val refreshToken = UUID.randomUUID().toString()
        val user: User = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found with username: $username") }

        userRepository.save<User>(user.copy(refreshToken = refreshToken))

        return refreshToken
    }

    /**
     * Обновляет токен доступа
     * @param [refreshToken] токен обновления
     * @return [RefreshTokenResponse] ответ
     */
    fun refreshAccessToken(refreshToken: String): RefreshTokenResponse {
        val user: User = userRepository
            .findByRefreshToken(refreshToken)
            .orElseThrow { RefreshTokenException("Refresh token not exists: $refreshToken") }

        return if (user.refreshToken.equals(refreshToken)) {
            val userDetails = userDetailsService.loadUserByUsername(user.username)
            RefreshTokenResponse(jwtService.generateToken(userDetails))
        } else {
            throw RefreshTokenException("Refresh token is not valid: $refreshToken")
        }
    }

    /**
     * Сохранение токена обновления для пользователя
     * @param username имя пользователя
     * @param refreshToken токен обновления
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun saveRefreshToken(username: String, refreshToken: String?) {
        val user: User = userRepository.findByUsername(username)
            .orElseThrow { EntityNotFoundException("User not found with username: $username") }
        userRepository.save(user.copy(refreshToken = refreshToken))
    }

    /**
     * Удаляет токен обновления у пользователя
     * @param [username] имя пользователя
     */
    fun deleteRefreshToken(username: String) {
        val user: User = userRepository
            .findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found with username: $username") }
        userRepository.save(user.copy(refreshToken = null))
    }
}