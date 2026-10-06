package com.srichaitanya.issuer.crypto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Service
class CryptoService(
    @Value("\${issuer.privateKeyBase64}") private val configuredPrivateKey: String
) {
    private val keyPair: KeyPair
    private val objectMapper = ObjectMapper().apply {
        configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    }

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
        keyPair = try {
            val kpg = KeyPairGenerator.getInstance("Ed25519")
            kpg.generateKeyPair()
        } catch (e: Exception) {
            val kpg = KeyPairGenerator.getInstance("Ed25519")
            kpg.generateKeyPair()
        }
    }

    fun getPublicKeyBase64(): String {
        return Base64.getEncoder().encodeToString(keyPair.public.encoded)
    }

    fun canonicalize(data: Map<String, Any?>): String {
        val sortedMap = TreeMap<String, Any?>()
        data.forEach { (k, v) ->
            if (v != null) {
                sortedMap[k] = v
            }
        }
        return objectMapper.writeValueAsString(sortedMap)
    }

    fun signCanonicalPayload(canonicalJson: String): String {
        val signer = Signature.getInstance("Ed25519")
        signer.initSign(keyPair.private)
        signer.update(canonicalJson.toByteArray(Charsets.UTF_8))
        val signatureBytes = signer.sign()
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    fun verifySignature(canonicalJson: String, signatureBase64: String, publicKeyBase64: String): Boolean {
        return try {
            val keyBytes = Base64.getDecoder().decode(publicKeyBase64)
            val keySpec = X509EncodedKeySpec(keyBytes)
            val kf = KeyFactory.getInstance("Ed25519")
            val pubKey = kf.generatePublic(keySpec)
            
            val verifier = Signature.getInstance("Ed25519")
            verifier.initVerify(pubKey)
            verifier.update(canonicalJson.toByteArray(Charsets.UTF_8))
            verifier.verify(Base64.getDecoder().decode(signatureBase64))
        } catch (e: Exception) {
            false
        }
    }
}
