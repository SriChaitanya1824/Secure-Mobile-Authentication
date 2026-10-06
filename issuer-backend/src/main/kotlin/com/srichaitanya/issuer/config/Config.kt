package com.srichaitanya.issuer.config

import com.srichaitanya.issuer.dto.ApiErrorResponse
import com.srichaitanya.issuer.security.JwtService
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthenticationFilter,
    private val rateLimitingFilter: RateLimitingFilter
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/issuers/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    .anyRequest().permitAll()
            }
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}

@Component
class JwtAuthenticationFilter(private val jwtService: JwtService) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            if (jwtService.validateToken(token)) {
                val userId = jwtService.extractUserId(token)
                val claims = jwtService.getClaims(token)
                val role = claims["role"] as? String ?: "HOLDER"
                if (userId != null) {
                    val auth = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_" + role))
                    )
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}

@Component
class RateLimitingFilter : OncePerRequestFilter() {
    private val requestCounts = ConcurrentHashMap<String, Pair<Long, AtomicInteger>>()
    private val MAX_REQUESTS_PER_MINUTE = 200

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientIp = request.remoteAddr ?: "unknown"
        val now = System.currentTimeMillis()
        val window = requestCounts.compute(clientIp) { _, existing ->
            if (existing == null || now - existing.first > 60_000) {
                Pair(now, AtomicInteger(1))
            } else {
                existing.second.incrementAndGet()
                existing
            }
        }

        if (window != null && window.second.get() > MAX_REQUESTS_PER_MINUTE) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write("{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests. Please try again later.\"}")
            return
        }

        filterChain.doFilter(request, response)
    }
}

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI().info(
            Info()
                .title("Secure Digital Certificate Wallet - Issuer API")
                .description("Production-grade Digital Identity & Certificate Authority Issuance API for VIDA sandbox portfolio.")
                .version("1.0.0")
                .license(License().name("Apache 2.0").url("https://springdoc.org"))
        )
    }
}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorResponse(
                code = "NOT_FOUND",
                message = e.message ?: "Resource not found",
                requestId = UUID.randomUUID().toString()
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse(
                code = "BAD_REQUEST",
                message = e.message ?: "Invalid request arguments",
                requestId = UUID.randomUUID().toString()
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(e: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val errors = e.bindingResult.fieldErrors.map { it.field + ": " + it.defaultMessage }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            ApiErrorResponse(
                code = "VALIDATION_FAILED",
                message = "Request validation failed",
                requestId = UUID.randomUUID().toString(),
                errors = errors
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralError(e: Exception): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiErrorResponse(
                code = "SERVER_ERROR",
                message = e.message ?: "An unexpected error occurred",
                requestId = UUID.randomUUID().toString()
            )
        )
    }
}
