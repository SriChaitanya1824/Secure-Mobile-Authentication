package com.srichaitanya.issuer.controller

import com.srichaitanya.issuer.crypto.CryptoService
import com.srichaitanya.issuer.dto.*
import com.srichaitanya.issuer.model.Issuer
import com.srichaitanya.issuer.repository.IssuerRepository
import com.srichaitanya.issuer.service.AuthService
import com.srichaitanya.issuer.service.CertificateService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.Instant

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, authentication and token refresh")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.refresh(request))
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate session")
    fun logout(authentication: Authentication?): ResponseEntity<Map<String, String>> {
        val userId = authentication?.name ?: "anonymous"
        authService.logout(userId)
        return ResponseEntity.ok(mapOf("status" to "LOGGED_OUT", "message" to "Successfully logged out"))
    }
}

@RestController
@RequestMapping("/api/issuer/certificates")
@Tag(name = "Certificate Issuance", description = "Endpoints for authorized issuers to issue and query certificates")
class IssuerCertificateController(private val certificateService: CertificateService) {

    @PostMapping
    @Operation(summary = "Issue and digitally sign a new certificate")
    fun issueCertificate(@Valid @RequestBody request: IssueCertificateRequest): ResponseEntity<CertificateDto> {
        return ResponseEntity.ok(certificateService.issueCertificate(request))
    }

    @GetMapping
    @Operation(summary = "List all certificates issued by the system")
    fun getAllIssued(): ResponseEntity<List<CertificateDto>> {
        return ResponseEntity.ok(certificateService.getAllIssuerCertificates())
    }
}

@RestController
@RequestMapping("/api/certificates")
@Tag(name = "Certificates", description = "Query individual certificates and perform revocation")
class CertificateController(private val certificateService: CertificateService) {

    @GetMapping("/{id}")
    @Operation(summary = "Get certificate by ID")
    fun getById(@PathVariable id: String): ResponseEntity<CertificateDto> {
        return ResponseEntity.ok(certificateService.getCertificateById(id))
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "Revoke an active certificate")
    fun revoke(
        @PathVariable id: String,
        @Valid @RequestBody request: RevokeCertificateRequest,
        principal: Principal?
    ): ResponseEntity<RevocationResponse> {
        val actor = request.revokedBy ?: principal?.name ?: "VIT Academic Credentials Registrar"
        return ResponseEntity.ok(certificateService.revokeCertificate(id, request.reason, actor))
    }
}

@RestController
@RequestMapping("/api/wallet/certificates")
@Tag(name = "Wallet", description = "Wallet holder certificate queries")
class WalletController(private val certificateService: CertificateService) {

    @GetMapping
    @Operation(summary = "Get all certificates owned by the wallet holder")
    fun getWalletCertificates(@RequestParam(required = false, defaultValue = "usr-student-001") subjectId: String): ResponseEntity<List<CertificateDto>> {
        return ResponseEntity.ok(certificateService.getWalletCertificates(subjectId))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single wallet certificate details")
    fun getWalletCertificateDetail(@PathVariable id: String): ResponseEntity<CertificateDto> {
        return ResponseEntity.ok(certificateService.getCertificateById(id))
    }
}

@RestController
@RequestMapping("/api/sync")
@Tag(name = "Sync", description = "Offline-first delta synchronization")
class SyncController(private val certificateService: CertificateService) {

    @GetMapping("/certificates")
    @Operation(summary = "Delta sync updated and revoked certificates")
    fun syncCertificates(@RequestParam(required = false) since: String?): ResponseEntity<SyncResponse> {
        val sinceInstant = since?.let { try { Instant.parse(it) } catch (e: Exception) { null } }
        return ResponseEntity.ok(certificateService.syncCertificates(sinceInstant))
    }
}

@RestController
@RequestMapping("/api/issuers")
@Tag(name = "Issuer Trust Registry", description = "Trusted issuer public keys and registry")
class IssuerRegistryController(
    private val issuerRepository: IssuerRepository,
    private val cryptoService: CryptoService
) {
    @GetMapping
    @Operation(summary = "List all trusted issuers")
    fun listIssuers(): ResponseEntity<List<IssuerDto>> {
        val issuers = issuerRepository.findAll().map {
            IssuerDto(it.id, it.issuerIdUri, it.name, it.publicKey, it.status)
        }
        return ResponseEntity.ok(issuers)
    }

    @GetMapping("/public-key")
    @Operation(summary = "Get the primary issuer public key for verification")
    fun getPrimaryPublicKey(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "issuerId" to "did:vid:issuer:vit-university",
            "algorithm" to "Ed25519",
            "publicKeyBase64" to cryptoService.getPublicKeyBase64()
        ))
    }
}
