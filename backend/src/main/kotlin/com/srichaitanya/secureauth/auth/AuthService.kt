package com.srichaitanya.secureauth.auth
import com.srichaitanya.secureauth.common.*
import com.srichaitanya.secureauth.otp.*
import com.srichaitanya.secureauth.security.JwtService
import com.srichaitanya.secureauth.session.*
import com.srichaitanya.secureauth.user.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.*
import java.time.Instant
import java.util.*
@Service class AuthService(private val users:UserRepository,private val otps:OtpRepository,private val refreshes:RefreshTokenRepository,private val encoder:PasswordEncoder,private val jwt:JwtService,private val limiter:RateLimiter,@Value("\${secureauth.otp-seconds}") private val otpTtl:Long,@Value("\${secureauth.refresh-seconds}") private val refreshTtl:Long,@Value("\${secureauth.otp-development-mode}") private val devOtp:Boolean){
 private val random=SecureRandom(); private val digest get()=MessageDigest.getInstance("SHA-256")
 private fun hash(value:String)=digest.digest(value.toByteArray()).joinToString(""){"%02x".format(it)}
 @Transactional fun register(r:RegisterRequest):ChallengeResponse { limiter.check("register:${r.email.lowercase()}",5,300); if(users.findByEmail(r.email.lowercase())!=null) throw ApiException("REGISTRATION_UNAVAILABLE",HttpStatus.CONFLICT,"Registration cannot be completed"); val u=users.save(User(email=r.email.lowercase(),passwordHash=encoder.encode(r.password),displayName=r.displayName.trim())); return challenge(u,"REGISTER") }
 @Transactional fun login(r:LoginRequest):ChallengeResponse { val email=r.email.lowercase(); limiter.check("login:$email",5,300); val u=users.findByEmail(email); if(u==null||!encoder.matches(r.password,u.passwordHash)) throw ApiException("INVALID_CREDENTIALS",HttpStatus.UNAUTHORIZED,"Email or password is incorrect"); if(u.locked) throw ApiException("ACCOUNT_LOCKED",HttpStatus.LOCKED,"Account is locked"); return challenge(u,"LOGIN") }
 private fun challenge(u:User,purpose:String):ChallengeResponse { val value=(random.nextInt(900000)+100000).toString(); val c=otps.save(OtpChallenge(userId=u.id,purpose=purpose,otpHash=encoder.encode(value),expiresAt=Instant.now().plusSeconds(otpTtl))); if(devOtp) System.err.println("DEV OTP challenge=${c.id} value=$value"); return ChallengeResponse(c.id.toString(),otpTtl,30) }
 @Transactional fun verify(r:OtpVerifyRequest):TokenResponse { limiter.check("otp:${r.challengeId}",8,300); val c=runCatching{otps.findById(UUID.fromString(r.challengeId)).orElse(null)}.getOrNull()?:throw ApiException("INVALID_OTP",HttpStatus.BAD_REQUEST,"The OTP is invalid or expired"); if(c.consumedAt!=null||c.expiresAt.isBefore(Instant.now())) throw ApiException("INVALID_OTP",HttpStatus.BAD_REQUEST,"The OTP is invalid or expired"); if(c.attempts>=c.maxAttempts) throw ApiException("OTP_ATTEMPTS_EXCEEDED",HttpStatus.LOCKED,"Maximum OTP attempts exceeded"); if(!encoder.matches(r.otp,c.otpHash)){ c.attempts++; otps.save(c); throw ApiException("INVALID_OTP",HttpStatus.BAD_REQUEST,"The OTP is invalid or expired") }; c.consumedAt=Instant.now(); otps.save(c); val u=users.findById(c.userId).orElseThrow(); u.active=true; users.save(u); return tokens(u.id) }
 @Transactional fun resend(r:OtpResendRequest):ChallengeResponse { val c=runCatching{otps.findById(UUID.fromString(r.challengeId)).orElse(null)}.getOrNull()?:throw ApiException("CHALLENGE_NOT_FOUND",HttpStatus.NOT_FOUND,"Challenge not found"); if(c.createdAt.plusSeconds(30).isAfter(Instant.now())) throw ApiException("RESEND_TOO_SOON",HttpStatus.TOO_MANY_REQUESTS,"Wait before requesting another OTP"); return challenge(users.findById(c.userId).orElseThrow(),c.purpose) }
 private fun tokens(userId:UUID):TokenResponse { val raw=Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(48).also(random::nextBytes)); refreshes.save(RefreshToken(userId=userId,tokenHash=hash(raw),expiresAt=Instant.now().plusSeconds(refreshTtl))); return TokenResponse(jwt.issue(userId),raw,300) }
 @Transactional fun refresh(r:RefreshRequest):TokenResponse { val old=refreshes.findByTokenHash(hash(r.refreshToken))?:throw ApiException("UNAUTHORIZED",HttpStatus.UNAUTHORIZED,"Refresh token is invalid"); if(old.revokedAt!=null||old.expiresAt.isBefore(Instant.now())) throw ApiException("SESSION_EXPIRED",HttpStatus.UNAUTHORIZED,"Session has expired"); old.revokedAt=Instant.now(); val next=tokens(old.userId); old.replacedBy=refreshes.findByTokenHash(hash(next.refreshToken))?.id; refreshes.save(old); return next }
 @Transactional fun logout(r:LogoutRequest){ r.refreshToken?.let{refreshes.findByTokenHash(hash(it))?.apply{revokedAt=Instant.now();refreshes.save(this)}} }
 fun me(id:UUID):UserResponse { val u=users.findById(id).orElseThrow{ApiException("UNAUTHORIZED",HttpStatus.UNAUTHORIZED,"Unauthorized")}; return UserResponse(u.id.toString(),u.email,u.displayName) }
}
