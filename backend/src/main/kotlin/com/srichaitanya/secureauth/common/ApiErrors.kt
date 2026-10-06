package com.srichaitanya.secureauth.common
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.*
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
data class ApiError(val code:String,val message:String,val requestId:String)
class ApiException(val code:String,val status:HttpStatus,val safeMessage:String):RuntimeException(safeMessage)
@RestControllerAdvice class ErrorHandler {
 @ExceptionHandler(ApiException::class) fun api(e:ApiException,r:HttpServletRequest)=ResponseEntity.status(e.status).body(ApiError(e.code,e.safeMessage,r.getAttribute("requestId")?.toString()?:"unknown"))
 @ExceptionHandler(MethodArgumentNotValidException::class) fun validation(e:Exception,r:HttpServletRequest)=ResponseEntity.badRequest().body(ApiError("VALIDATION_ERROR","Request validation failed",r.getAttribute("requestId")?.toString()?:"unknown"))
 @ExceptionHandler(Exception::class) fun unknown(e:Exception,r:HttpServletRequest)=ResponseEntity.status(500).body(ApiError("INTERNAL_ERROR","An unexpected error occurred",r.getAttribute("requestId")?.toString()?:"unknown"))
}
