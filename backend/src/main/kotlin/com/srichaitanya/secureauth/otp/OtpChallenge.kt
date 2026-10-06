package com.srichaitanya.secureauth.otp
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID
@Entity @Table(name="otp_challenges") class OtpChallenge(@Id var id:UUID=UUID.randomUUID(), @Column(name="user_id") var userId:UUID=UUID.randomUUID(), var purpose:String="LOGIN", @Column(name="otp_hash") var otpHash:String="", @Column(name="expires_at") var expiresAt:Instant=Instant.now(), var attempts:Int=0, @Column(name="max_attempts") var maxAttempts:Int=5, @Column(name="consumed_at") var consumedAt:Instant?=null, @Column(name="created_at") var createdAt:Instant=Instant.now())
interface OtpRepository:org.springframework.data.jpa.repository.JpaRepository<OtpChallenge,UUID>
