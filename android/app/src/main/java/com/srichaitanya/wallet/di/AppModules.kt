package com.srichaitanya.wallet.di

import android.content.Context
import androidx.room.Room
import com.srichaitanya.wallet.core.database.WalletDatabase
import com.srichaitanya.wallet.core.database.dao.CertificateDao
import com.srichaitanya.wallet.core.database.dao.ClaimDao
import com.srichaitanya.wallet.core.database.entity.CertificateEntity
import com.srichaitanya.wallet.core.database.entity.ClaimEntity
import com.srichaitanya.wallet.core.model.*
import com.srichaitanya.wallet.core.network.api.IssuerApi
import com.srichaitanya.wallet.core.network.api.VerifierApi
import com.srichaitanya.wallet.core.network.interceptor.AuthInterceptor
import com.srichaitanya.wallet.core.security.biometric.BiometricAuthManager
import com.srichaitanya.wallet.core.security.keystore.KeystoreManager
import com.srichaitanya.wallet.core.security.storage.SecureStorage
import com.srichaitanya.wallet.core.usecase.CertificateRepository
import com.srichaitanya.wallet.core.common.Resource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    @Provides
    @Singleton
    fun provideKeystoreManager(@ApplicationContext context: Context): KeystoreManager {
        return KeystoreManager(context)
    }

    @Provides
    @Singleton
    fun provideBiometricAuthManager(@ApplicationContext context: Context): BiometricAuthManager {
        return BiometricAuthManager(context)
    }

    @Provides
    @Singleton
    fun provideSecureStorage(@ApplicationContext context: Context): SecureStorage {
        return SecureStorage(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideWalletDatabase(@ApplicationContext context: Context): WalletDatabase {
        return Room.databaseBuilder(
            context,
            WalletDatabase::class.java,
            "wallet_secure.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCertificateDao(db: WalletDatabase): CertificateDao = db.certificateDao()

    @Provides
    fun provideClaimDao(db: WalletDatabase): ClaimDao = db.claimDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(secureStorage: SecureStorage): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { secureStorage.getToken() })
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideIssuerApi(client: OkHttpClient): IssuerApi {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8081")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(IssuerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVerifierApi(client: OkHttpClient): VerifierApi {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8082")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(VerifierApi::class.java)
    }
}

class DefaultCertificateRepository(
    private val certDao: CertificateDao,
    private val claimDao: ClaimDao,
    private val issuerApi: IssuerApi
) : CertificateRepository {

    override fun getCertificates(): Flow<List<Certificate>> {
        return certDao.getAllCertificates().map { entities ->
            if (entities.isEmpty()) {
                // Seed initial sample certificate for demo
                listOf(
                    Certificate(
                        id = "CERT-VIDA-SAMPLE-01",
                        credentialType = CredentialType.NATIONAL_ID,
                        issuerId = "VIDA-ROOT-CA",
                        issuerName = "VIDA Digital Identity Authority",
                        holderDid = "did:vida:holder123",
                        subjectName = "Jane Doe",
                        subjectIdentifier = "3171020000000001",
                        issuedAt = System.currentTimeMillis() - 86400000,
                        expiresAt = System.currentTimeMillis() + (365L * 86400000),
                        status = CertificateStatus.ACTIVE,
                        signatureAlgorithm = "Ed25519",
                        signatureValue = "98234fedcba...",
                        canonicalHash = "canonical-hash-sample",
                        claims = listOf(
                            Claim("fullName", "Full Legal Name", "Jane Doe"),
                            Claim("birthDate", "Date of Birth", "1995-08-17", isSensitive = true),
                            Claim("nationality", "Nationality", "ID"),
                            Claim("gender", "Gender", "FEMALE")
                        )
                    )
                )
            } else {
                entities.map { e ->
                    val claims = claimDao.getClaimsForCertificate(e.id).map { c ->
                        Claim(c.key, c.label, c.value, c.isSensitive)
                    }
                    Certificate(
                        id = e.id,
                        credentialType = CredentialType.valueOf(e.credentialType),
                        issuerId = e.issuerId,
                        issuerName = e.issuerName,
                        holderDid = e.holderDid,
                        subjectName = e.subjectName,
                        subjectIdentifier = e.subjectIdentifier,
                        issuedAt = e.issuedAt,
                        expiresAt = e.expiresAt,
                        status = CertificateStatus.valueOf(e.status),
                        signatureAlgorithm = e.signatureAlgorithm,
                        signatureValue = e.signatureValue,
                        canonicalHash = e.canonicalHash,
                        claims = claims,
                        isStoredInHardwareKeystore = e.isStoredInHardwareKeystore
                    )
                }
            }
        }
    }

    override suspend fun getCertificateById(id: String): Certificate? {
        val e = certDao.getCertificateById(id)
        return if (e != null) {
            val claims = claimDao.getClaimsForCertificate(e.id).map { c ->
                Claim(c.key, c.label, c.value, c.isSensitive)
            }
            Certificate(
                id = e.id,
                credentialType = CredentialType.valueOf(e.credentialType),
                issuerId = e.issuerId,
                issuerName = e.issuerName,
                holderDid = e.holderDid,
                subjectName = e.subjectName,
                subjectIdentifier = e.subjectIdentifier,
                issuedAt = e.issuedAt,
                expiresAt = e.expiresAt,
                status = CertificateStatus.valueOf(e.status),
                signatureAlgorithm = e.signatureAlgorithm,
                signatureValue = e.signatureValue,
                canonicalHash = e.canonicalHash,
                claims = claims,
                isStoredInHardwareKeystore = e.isStoredInHardwareKeystore
            )
        } else {
            // Demo fallback certificate
            Certificate(
                id = id,
                credentialType = CredentialType.NATIONAL_ID,
                issuerId = "VIDA-ROOT-CA",
                issuerName = "VIDA Digital Identity Authority",
                holderDid = "did:vida:holder123",
                subjectName = "Jane Doe",
                subjectIdentifier = "3171020000000001",
                issuedAt = System.currentTimeMillis() - 86400000,
                expiresAt = System.currentTimeMillis() + (365L * 86400000),
                status = CertificateStatus.ACTIVE,
                signatureAlgorithm = "Ed25519",
                signatureValue = "98234fedcba...",
                canonicalHash = "canonical-hash-sample",
                claims = listOf(
                    Claim("fullName", "Full Legal Name", "Jane Doe"),
                    Claim("birthDate", "Date of Birth", "1995-08-17", isSensitive = true),
                    Claim("nationality", "Nationality", "ID"),
                    Claim("gender", "Gender", "FEMALE")
                )
            )
        }
    }

    override suspend fun saveCertificate(certificate: Certificate) {
        val entity = CertificateEntity(
            id = certificate.id,
            credentialType = certificate.credentialType.name,
            issuerId = certificate.issuerId,
            issuerName = certificate.issuerName,
            holderDid = certificate.holderDid,
            subjectName = certificate.subjectName,
            subjectIdentifier = certificate.subjectIdentifier,
            issuedAt = certificate.issuedAt,
            expiresAt = certificate.expiresAt,
            status = certificate.status.name,
            signatureAlgorithm = certificate.signatureAlgorithm,
            signatureValue = certificate.signatureValue,
            canonicalHash = certificate.canonicalHash,
            isStoredInHardwareKeystore = certificate.isStoredInHardwareKeystore
        )
        val claims = certificate.claims.map { c ->
            ClaimEntity(certificate.id, c.key, c.label, c.value, c.isSensitive)
        }
        certDao.insertCertificate(entity)
        claimDao.insertClaims(claims)
    }

    override suspend fun deleteCertificate(id: String) {
        certDao.deleteCertificate(id)
        claimDao.deleteClaimsForCertificate(id)
    }

    override suspend fun updateCertificateStatus(id: String, status: CertificateStatus) {
        certDao.updateStatus(id, status.name)
    }

    override suspend fun syncWithIssuer(token: String): Resource<List<Certificate>> {
        return try {
            val response = issuerApi.getMyCertificates()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(emptyList())
            } else {
                Resource.Error("Sync failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sync error")
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCertificateRepository(
        certDao: CertificateDao,
        claimDao: ClaimDao,
        issuerApi: IssuerApi
    ): CertificateRepository {
        return DefaultCertificateRepository(certDao, claimDao, issuerApi)
    }
}
