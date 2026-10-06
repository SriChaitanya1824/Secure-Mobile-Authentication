package com.srichaitanya.secureauth.network
import kotlinx.serialization.Serializable
import retrofit2.http.*
@Serializable data class LoginDto(val email:String,val password:String)
@Serializable data class RegisterDto(val email:String,val password:String,val displayName:String)
@Serializable data class OtpDto(val challengeId:String,val otp:String)
@Serializable data class RefreshDto(val refreshToken:String)
@Serializable data class LogoutDto(val refreshToken:String?)
@Serializable data class ChallengeDto(val challengeId:String,val expiresInSeconds:Long,val resendAfterSeconds:Long)
@Serializable data class TokenDto(val accessToken:String,val refreshToken:String,val expiresInSeconds:Long)
@Serializable data class UserDto(val id:String,val email:String,val displayName:String)
interface AuthApi { @POST("api/auth/login") suspend fun login(@Body body:LoginDto):ChallengeDto; @POST("api/auth/register") suspend fun register(@Body body:RegisterDto):ChallengeDto; @POST("api/auth/otp/verify") suspend fun verify(@Body body:OtpDto):TokenDto; @POST("api/auth/refresh") suspend fun refresh(@Body body:RefreshDto):TokenDto; @POST("api/auth/logout") suspend fun logout(@Body body:LogoutDto); @GET("api/users/me") suspend fun me():UserDto }
