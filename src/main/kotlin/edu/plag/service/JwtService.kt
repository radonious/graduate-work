package edu.plag.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Function
import javax.crypto.SecretKey

@Service
class JwtService {
    /**
     * Проверяет токен на корректность
     * @param token токен
     * @param userDetails данные пользователя
     * @return true - валидный, false - невалидный
     */
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    /**
     * Проверяет, закончился ли срок действия токена доступа
     * @param token токен
     * @return true - закончился, false - не закончился
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            extractAllClaims(token)
            false
        } catch (e: ExpiredJwtException) {
            true
        }
    }

    /**
     * Определяет дату истечения токена
     * @param token токен
     * @return дата истечения
     */
    private fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    /**
     * Генерирует новый токен для пользователя
     * @param userDetails информация о пользователе
     * @return сгенерированный токен
     */
    fun generateToken(userDetails: UserDetails): String {
        val claims: Map<String, Any> = HashMap()
        return createToken(claims, userDetails.username)
    }

    /**
     * Создает токен по параметрам
     * @param claims параметры
     * @param subject пользователь
     * @return полученный токен
     */
    fun createToken(claims: Map<String, Any>, subject: String): String {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + MS_PER_HOUR * TOKEN_DEFAULT_HOURS_LENGTH))
            .signWith(SECRET_KEY, Jwts.SIG.HS256)
            .compact()
    }

    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    fun extractAllClaims(token: String): Claims {
        return Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).payload
    }

    companion object {
        private const val TOKEN_DEFAULT_HOURS_LENGTH = 10
        private const val MS_PER_HOUR = 1000 * 60 * 60
        private val SECRET_KEY: SecretKey = Jwts.SIG.HS256.key().build()
    }
}