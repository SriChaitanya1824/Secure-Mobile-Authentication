package com.srichaitanya.wallet.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey
    val id: String,
    val credentialType: String,
    val issuerId: String,
    val issuerName: String,
    val holderDid: String,
    val subjectName: String,
    val subjectIdentifier: String,
    val issuedAt: Long,
    val expiresAt: Long,
    val status: String,
    val signatureAlgorithm: String,
    val signatureValue: String,
    val canonicalHash: String,
    val isStoredInHardwareKeystore: Boolean = true
)

@Entity(tableName = "claims", primaryKeys = ["certificateId", "key"])
data class ClaimEntity(
    val certificateId: String,
    val key: String,
    val label: String,
    val value: String,
    val isSensitive: Boolean
)

@Entity(tableName = "audit_logs")
data class AuditEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val eventType: String,
    val details: String,
    val status: String
)
