package com.srichaitanya.secureauth.session
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID
@Entity @Table(name="refresh_tokens") class RefreshToken(@Id var id:UUID=UUID.randomUUID(), @Column(name="user_id") var userId:UUID=UUID.randomUUID(), @Column(name="token_hash",unique=true) var tokenHash:String="", @Column(name="expires_at") var expiresAt:Instant=Instant.now(), @Column(name="revoked_at") var revokedAt:Instant?=null, @Column(name="replaced_by") var replacedBy:UUID?=null, @Column(name="created_at") var createdAt:Instant=Instant.now())
interface RefreshTokenRepository:org.springframework.data.jpa.repository.JpaRepository<RefreshToken,UUID> { fun findByTokenHash(tokenHash:String):RefreshToken? }
