package com.srichaitanya.secureauth.common
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
@Component class RateLimiter { private data class Window(var start:Instant,var count:Int); private val buckets=ConcurrentHashMap<String,Window>()
 @Synchronized fun check(key:String,limit:Int,seconds:Long){ val now=Instant.now(); val w=buckets.compute(key){_,old->if(old==null||old.start.plusSeconds(seconds).isBefore(now)) Window(now,1) else old.apply{count++}}!!; if(w.count>limit) throw ApiException("RATE_LIMITED",org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,"Too many attempts; try again later") }
}
