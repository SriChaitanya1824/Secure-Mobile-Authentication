package com.srichaitanya.wallet.core.network.api

import com.srichaitanya.wallet.core.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface IssuerApi {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("/api/certificates/my")
    suspend fun getMyCertificates(): Response<List<NetworkCertificate>>

    @GET("/api/issuers")
    suspend fun getIssuers(): Response<List<NetworkIssuer>>

    @GET("/api/certificates/{id}/status")
    suspend fun checkStatus(@Path("id") certId: String): Response<Map<String, String>>
}

interface VerifierApi {
    @POST("/api/verify/presentation")
    suspend fun verifyPresentation(@Body request: VerifyPresentationRequest): Response<NetworkVerificationResponse>
}
