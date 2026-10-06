package com.srichaitanya.secureauth.security
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*
@Service class JwtService(@Value("\${secureauth.jwt-secret}") secret:String,@Value("\${secureauth.access-seconds}") private val ttl:Long){ private val key=Keys.hmacShaKeyFor(secret.padEnd(32,'0').toByteArray(StandardCharsets.UTF_8))
 fun issue(userId:UUID)=Jwts.builder().subject(userId.toString()).issuedAt(Date()).expiration(Date.from(Instant.now().plusSeconds(ttl))).id(UUID.randomUUID().toString()).signWith(key).compact()
 fun userId(token:String)=UUID.fromString(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject)
}
