package com.srichaitanya.secureauth.data
import com.srichaitanya.secureauth.api.*
import com.srichaitanya.secureauth.network.*
import com.srichaitanya.secureauth.storage.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
class DefaultSecureAuthClient(private val api:AuthApi,private val storage:TokenStorage):SecureAuthClient { private val _state=MutableStateFlow<SessionState>(if(storage.read()!=null) SessionState.Authenticated(null) else SessionState.Unauthenticated); override val sessionState:StateFlow<SessionState> = _state.asStateFlow(); private val refreshMutex=Mutex(); @Volatile private var user:AuthUser?=null
 override suspend fun login(email:String,password:String):AuthResult<Unit>{_state.value=SessionState.Authenticating;return call{val c=api.login(LoginDto(email,password));_state.value=SessionState.OtpRequired(c.challengeId);AuthResult.OtpRequired(c.challengeId,c.expiresInSeconds)}}
 override suspend fun register(email:String,password:String,displayName:String):AuthResult<Unit>{_state.value=SessionState.Authenticating;return call{val c=api.register(RegisterDto(email,password,displayName));_state.value=SessionState.OtpRequired(c.challengeId);AuthResult.OtpRequired(c.challengeId,c.expiresInSeconds)}}
 override suspend fun verifyOtp(challengeId:String,otp:String):AuthResult<AuthUser?> = call { save(api.verify(OtpDto(challengeId,otp))); val u=runCatching{api.me()}.getOrNull()?.let{AuthUser(it.id,it.email,it.displayName)};user=u;_state.value=SessionState.Authenticated(u);AuthResult.Success(u) }
 override suspend fun refreshSession():AuthResult<Unit> = refreshMutex.withLock { val current=storage.read()?:return@withLock AuthResult.SessionExpired; if(current.expiresAtEpochSeconds>Instant.now().epochSecond+30)return@withLock AuthResult.Success(Unit);_state.value=SessionState.Refreshing;call{save(api.refresh(RefreshDto(current.refresh)));_state.value=SessionState.Authenticated(user);AuthResult.Success(Unit)} }
 private fun save(t:TokenDto)=storage.write(StoredTokens(t.accessToken,t.refreshToken,Instant.now().epochSecond+t.expiresInSeconds))
 override suspend fun logout(){val t=storage.read();storage.clear();user=null;_state.value=SessionState.Unauthenticated;runCatching{api.logout(LogoutDto(t?.refresh))}}
 override suspend fun getCurrentUser():AuthUser?{ if(user==null&&storage.read()!=null) user=runCatching{api.me()}.getOrNull()?.let{AuthUser(it.id,it.email,it.displayName)};return user }
 override fun isAuthenticated()=storage.read()!=null
 private suspend fun <T> call(block:suspend()->AuthResult<T>):AuthResult<T> = try{block()}catch(e:HttpException){val r:AuthResult<Nothing>=when(e.code()){401->AuthResult.InvalidCredentials;423->AuthResult.AccountLocked;429->AuthResult.RateLimited;in 500..599->AuthResult.NetworkError(true);else->AuthResult.UnknownError(e.response()?.headers()?.get("X-Request-ID"))};_state.value=SessionState.Error(r);r}catch(_:IOException){val r=AuthResult.NetworkError(true);_state.value=SessionState.Error(r);r}
}
