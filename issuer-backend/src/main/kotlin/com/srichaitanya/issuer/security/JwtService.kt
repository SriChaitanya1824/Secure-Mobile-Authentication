package com.srichaitanya.issuer.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expirationMs}") val expirationMs: Long,
    @Value("\${jwt.refreshExpirationMs}") val refreshExpirationMs: Long
) {
    private val key: SecretKey by lazy {
        val secretBytes = secret.padEnd(32, '0').toByteArray(StandardCharsets.UTF_8)
        Keys.hmacShaKeyFor(secretBytes)
    }

    fun generateAccessToken(userId: String, email: String, role: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)
        return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun extractUserId(token: String): String? {
        return try {
            getClaims(token).subject
        } catch (e: Exception) {
            null
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
