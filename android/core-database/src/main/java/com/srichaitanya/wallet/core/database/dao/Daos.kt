package com.srichaitanya.wallet.core.database.dao

import androidx.room.*
import com.srichaitanya.wallet.core.database.entity.AuditEntity
import com.srichaitanya.wallet.core.database.entity.CertificateEntity
import com.srichaitanya.wallet.core.database.entity.ClaimEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {
    @Query("SELECT * FROM certificates ORDER BY issuedAt DESC")
    fun getAllCertificates(): Flow<List<CertificateEntity>>

    @Query("SELECT * FROM certificates WHERE id = :id")
    suspend fun getCertificateById(id: String): CertificateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(cert: CertificateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificates(certs: List<CertificateEntity>)

    @Query("UPDATE certificates SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM certificates WHERE id = :id")
    suspend fun deleteCertificate(id: String)
}

@Dao
interface ClaimDao {
    @Query("SELECT * FROM claims WHERE certificateId = :certificateId")
    suspend fun getClaimsForCertificate(certificateId: String): List<ClaimEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaims(claims: List<ClaimEntity>)

    @Query("DELETE FROM claims WHERE certificateId = :certificateId")
    suspend fun deleteClaimsForCertificate(certificateId: String)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: AuditEntity)
}
