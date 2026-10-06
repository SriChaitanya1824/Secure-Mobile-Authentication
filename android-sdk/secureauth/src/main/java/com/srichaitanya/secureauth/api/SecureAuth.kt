package com.srichaitanya.secureauth.api
import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.srichaitanya.secureauth.data.DefaultSecureAuthClient
import com.srichaitanya.secureauth.network.AuthApi
import com.srichaitanya.secureauth.storage.*
import kotlinx.serialization.json.Json
import okhttp3.*
import retrofit2.Retrofit
import java.util.UUID
import java.util.concurrent.TimeUnit
object SecureAuth { @Volatile private var client:SecureAuthClient?=null
 @Synchronized fun initialize(context:Context,configuration:SecureAuthConfiguration):SecureAuthClient { client?.let{return it}; val storage=KeystoreTokenStorage(context.applicationContext); val auth=Interceptor{chain->val token=storage.read()?.access;val b=chain.request().newBuilder().header("X-Request-ID",UUID.randomUUID().toString());if(token!=null)b.header("Authorization","Bearer $token");chain.proceed(b.build())};val http=OkHttpClient.Builder().connectTimeout(configuration.connectTimeoutSeconds,TimeUnit.SECONDS).readTimeout(configuration.readTimeoutSeconds,TimeUnit.SECONDS).addInterceptor(auth).build();val api=Retrofit.Builder().baseUrl(configuration.baseUrl).client(http).addConverterFactory(Json{ignoreUnknownKeys=true}.asConverterFactory("application/json".toMediaType())).build().create(AuthApi::class.java);return DefaultSecureAuthClient(api,storage).also{client=it} }
 fun get():SecureAuthClient=checkNotNull(client){"SecureAuth.initialize must be called first"}
 suspend fun login(email:String,password:String)=get().login(email,password)
 suspend fun register(email:String,password:String,displayName:String)=get().register(email,password,displayName)
 suspend fun verifyOtp(challengeId:String,otp:String)=get().verifyOtp(challengeId,otp)
 suspend fun refreshToken()=get().refreshSession()
 suspend fun logout()=get().logout()
 suspend fun getCurrentUser()=get().getCurrentUser()
 fun isAuthenticated()=get().isAuthenticated()
 fun getSessionState()=get().sessionState
}
