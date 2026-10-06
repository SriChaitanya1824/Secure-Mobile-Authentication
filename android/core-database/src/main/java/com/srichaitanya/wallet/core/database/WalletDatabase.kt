package com.srichaitanya.wallet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.srichaitanya.wallet.core.database.dao.AuditDao
import com.srichaitanya.wallet.core.database.dao.CertificateDao
import com.srichaitanya.wallet.core.database.dao.ClaimDao
import com.srichaitanya.wallet.core.database.entity.AuditEntity
import com.srichaitanya.wallet.core.database.entity.CertificateEntity
import com.srichaitanya.wallet.core.database.entity.ClaimEntity

@Database(
    entities = [CertificateEntity::class, ClaimEntity::class, AuditEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun certificateDao(): CertificateDao
    abstract fun claimDao(): ClaimDao
    abstract fun auditDao(): AuditDao
}
