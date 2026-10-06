package com.srichaitanya.secureauth
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
@SpringBootTest @AutoConfigureMockMvc class AuthIntegrationTest(@Autowired val mvc:MockMvc){
 @Test fun `registration creates an otp challenge`(){ mvc.post("/api/auth/register"){contentType=MediaType.APPLICATION_JSON;content="""{"email":"new@example.com","password":"StrongPassword1!","displayName":"New User"}"""}.andExpect{status{isCreated()};jsonPath("$.challengeId"){exists()}} }
 @Test fun `login does not enumerate unknown accounts`(){ mvc.post("/api/auth/login"){contentType=MediaType.APPLICATION_JSON;content="""{"email":"missing@example.com","password":"WrongPassword1!"}"""}.andExpect{status{isUnauthorized()};jsonPath("$.code"){value("INVALID_CREDENTIALS")}} }
}
