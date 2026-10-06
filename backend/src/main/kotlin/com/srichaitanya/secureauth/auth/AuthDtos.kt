package com.srichaitanya.secureauth.auth
import jakarta.validation.constraints.*
data class RegisterRequest(@field:Email val email:String,@field:Size(min=12,max=72) val password:String,@field:Size(min=2,max=100) val displayName:String)
data class LoginRequest(@field:Email val email:String,@field:NotBlank val password:String)
data class OtpVerifyRequest(val challengeId:String,@field:Pattern(regexp="\\d{6}") val otp:String)
data class OtpResendRequest(val challengeId:String)
data class RefreshRequest(@field:NotBlank val refreshToken:String)
data class LogoutRequest(val refreshToken:String?)
data class ChallengeResponse(val challengeId:String,val expiresInSeconds:Long,val resendAfterSeconds:Long)
data class TokenResponse(val accessToken:String,val refreshToken:String,val expiresInSeconds:Long,val tokenType:String="Bearer")
data class UserResponse(val id:String,val email:String,val displayName:String)
