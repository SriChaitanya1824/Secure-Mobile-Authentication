package com.srichaitanya.secureauth.auth
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID
@RestController @RequestMapping("/api/auth") class AuthController(private val service:AuthService){
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) fun register(@Valid @RequestBody r:RegisterRequest)=service.register(r)
 @PostMapping("/login") fun login(@Valid @RequestBody r:LoginRequest)=service.login(r)
 @PostMapping("/otp/verify") fun verify(@Valid @RequestBody r:OtpVerifyRequest)=service.verify(r)
 @PostMapping("/otp/resend") fun resend(@Valid @RequestBody r:OtpResendRequest)=service.resend(r)
 @PostMapping("/refresh") fun refresh(@Valid @RequestBody r:RefreshRequest)=service.refresh(r)
 @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) fun logout(@RequestBody r:LogoutRequest)=service.logout(r)
 @PostMapping("/biometric/session") fun biometric(authentication:Authentication)=service.me(authentication.principal as UUID)
}
@RestController class UserController(private val service:AuthService){ @GetMapping("/api/users/me") fun me(authentication:Authentication)=service.me(authentication.principal as UUID); @GetMapping("/api/health") fun health()=mapOf("status" to "UP") }
