package com.srichaitanya.secureauth.common
import jakarta.servlet.*
import jakarta.servlet.http.*
import org.springframework.stereotype.Component
import java.util.UUID
@Component class RequestIdFilter:Filter { override fun doFilter(req:ServletRequest,res:ServletResponse,chain:FilterChain){ val id=(req as HttpServletRequest).getHeader("X-Request-ID")?.take(100)?:UUID.randomUUID().toString(); req.setAttribute("requestId",id); (res as HttpServletResponse).setHeader("X-Request-ID",id); chain.doFilter(req,res) } }
