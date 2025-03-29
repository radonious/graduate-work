package edu.plag.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
data class SecurityConfig(
    private val jwtRequestFilter: JwtRequestFilter,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
) {
    companion object {
        private val PUBLIC_PATHS = arrayOf(
            "/api/v1/auth/**"
        )

        private val SECURED_PATHS = arrayOf(
            "/api/v1/users/**",
            "/api/v1/forms/**",
            "/api/v1/completions/**",
            "/api/v1/statistic/**"
        )
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    @Throws(Exception::class)
    fun publicFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.securityMatcher(*PUBLIC_PATHS)
            .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
            .cors { cors: CorsConfigurer<HttpSecurity> -> cors.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { request -> request.anyRequest().permitAll() }
        return http.build()
    }

    @Bean
    @Throws(Exception::class)
    fun securedFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.securityMatcher(*SECURED_PATHS)
            .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
            .cors { cors: CorsConfigurer<HttpSecurity> -> cors.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { request -> request.anyRequest().authenticated() }
            .exceptionHandling { exceptionHandling: ExceptionHandlingConfigurer<HttpSecurity> ->
                exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            }
            .sessionManagement { sessionManagement: SessionManagementConfigurer<HttpSecurity> ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }


    @Bean
    @Throws(Exception::class)
    fun authenticationManager(
        authenticationConfiguration: AuthenticationConfiguration
    ): AuthenticationManager {
        return authenticationConfiguration.getAuthenticationManager()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.setAllowedMethods(listOf("*"))
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = false
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}