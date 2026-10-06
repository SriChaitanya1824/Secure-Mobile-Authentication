package com.srichaitanya.secureauth.config
import com.srichaitanya.secureauth.security.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.*
import org.springframework.context.annotation.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
@Component class JwtFilter(private val jwt:JwtService):OncePerRequestFilter(){ override fun doFilterInternal(r:HttpServletRequest,s:HttpServletResponse,c:FilterChain){ val raw=r.getHeader("Authorization")?.takeIf{it.startsWith("Bearer ")}?.removePrefix("Bearer "); if(raw!=null) try { val id=jwt.userId(raw); SecurityContextHolder.getContext().authentication=UsernamePasswordAuthenticationToken(id,null,emptyList()) }catch(_:Exception){}; c.doFilter(r,s) } }
@Configuration class SecurityConfig { @Bean fun encoder()=BCryptPasswordEncoder(12); @Bean fun chain(http:HttpSecurity,jwt:JwtFilter):SecurityFilterChain=http.csrf{it.disable()}.sessionManagement{it.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)}.authorizeHttpRequests{it.requestMatchers("/api/auth/biometric/session").authenticated().requestMatchers("/api/auth/**","/api/health","/swagger-ui/**","/v3/api-docs/**").permitAll().anyRequest().authenticated()}.addFilterBefore(jwt,UsernamePasswordAuthenticationFilter::class.java).build() }
