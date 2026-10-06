package com.srichaitanya.issuer

import com.srichaitanya.issuer.crypto.CryptoService
import com.srichaitanya.issuer.dto.IssueCertificateRequest
import com.srichaitanya.issuer.dto.LoginRequest
import com.srichaitanya.issuer.dto.RegisterRequest
import com.srichaitanya.issuer.model.CertificateStatus
import com.srichaitanya.issuer.model.UserRole
import com.srichaitanya.issuer.service.AuthService
import com.srichaitanya.issuer.service.CertificateService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class IssuerBackendTests {

    @Autowired
    private lateinit var cryptoService: CryptoService

    @Autowired
    private lateinit var certificateService: CertificateService

    @Autowired
    private lateinit var authService: AuthService

    @Test
    fun `test Ed25519 signing and verification`() {
        val payloadMap = mapOf(
            "certificateId" to "cert-test-001",
            "credentialType" to "University Degree",
            "subjectName" to "John Doe",
            "issuedAt" to "2026-01-01T00:00:00Z"
        )
        val canonicalJson = cryptoService.canonicalize(payloadMap)
        val signature = cryptoService.signCanonicalPayload(canonicalJson)
        val pubKey = cryptoService.getPublicKeyBase64()

        assertTrue(signature.isNotBlank())
        val verified = cryptoService.verifySignature(canonicalJson, signature, pubKey)
        assertTrue(verified, "Signature must verify against generated canonical JSON")

        val tamperedJson = canonicalJson.replace("John Doe", "Jane Doe")
        val tamperedVerify = cryptoService.verifySignature(tamperedJson, signature, pubKey)
        assertFalse(tamperedVerify, "Tampered payload must fail signature verification")
    }

    @Test
    fun `test issue and retrieve certificate lifecycle`() {
        val issueReq = IssueCertificateRequest(
            credentialType = "University Degree",
            subjectId = "usr-test-student",
            subjectName = "Alice Smith",
            claims = mapOf(
                "degree" to "B.Tech in Artificial Intelligence",
                "cgpa" to "9.8"
            )
        )

        val certDto = certificateService.issueCertificate(issueReq)
        assertNotNull(certDto.certificateId)
        assertEquals("University Degree", certDto.credentialType)
        assertEquals(CertificateStatus.ACTIVE, certDto.status)
        assertEquals("Alice Smith", certDto.subjectName)
        assertEquals("9.8", certDto.claims["cgpa"])

        val fetched = certificateService.getCertificateById(certDto.certificateId)
        assertEquals(certDto.certificateId, fetched.certificateId)
    }

    @Test
    fun `test idempotent revocation`() {
        val issueReq = IssueCertificateRequest(
            credentialType = "Employment Certificate",
            subjectId = "usr-test-emp",
            subjectName = "Bob Builder",
            claims = mapOf("role" to "Senior Software Engineer")
        )
        val cert = certificateService.issueCertificate(issueReq)
        assertEquals(CertificateStatus.ACTIVE, cert.status)

        val rev1 = certificateService.revokeCertificate(cert.certificateId, "Contract Completed", "Admin")
        assertEquals(CertificateStatus.REVOKED, rev1.status)
        assertEquals("Contract Completed", rev1.reason)

        val rev2 = certificateService.revokeCertificate(cert.certificateId, "Contract Completed", "Admin")
        assertEquals(CertificateStatus.REVOKED, rev2.status)
        assertEquals("Contract Completed", rev2.reason)
    }

    @Test
    fun `test user registration and login flow`() {
        val email = "alice_" + System.currentTimeMillis() + "@test.com"
        val regReq = RegisterRequest(
            fullName = "Alice In Chains",
            email = email,
            password = "Password@123",
            role = UserRole.HOLDER
        )
        val authResp = authService.register(regReq)
        assertNotNull(authResp.accessToken)
        assertNotNull(authResp.refreshToken)
        assertEquals(email, authResp.user.email)

        val loginResp = authService.login(LoginRequest(email, "Password@123"))
        assertNotNull(loginResp.accessToken)
    }
}
