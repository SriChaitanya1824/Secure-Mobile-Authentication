package com.srichaitanya.verifier.repository

import com.srichaitanya.verifier.model.TrustedIssuer
import com.srichaitanya.verifier.model.VerificationRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VerificationRecordRepository : JpaRepository<VerificationRecord, Long> {
    fun findByEventId(eventId: String): Optional<VerificationRecord>
    fun existsByNonce(nonce: String): Boolean
    fun findTop50ByOrderByVerifiedAtDesc(): List<VerificationRecord>
}

@Repository
interface TrustedIssuerRepository : JpaRepository<TrustedIssuer, String>
