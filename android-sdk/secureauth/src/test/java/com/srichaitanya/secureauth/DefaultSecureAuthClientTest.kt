package com.srichaitanya.secureauth
import com.srichaitanya.secureauth.api.*
import com.srichaitanya.secureauth.data.DefaultSecureAuthClient
import com.srichaitanya.secureauth.network.*
import com.srichaitanya.secureauth.storage.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy
class DefaultSecureAuthClientTest { private class MemoryStorage:TokenStorage{var value:StoredTokens?=null;override fun read()=value;override fun write(tokens:StoredTokens){value=tokens};override fun clear(){value=null}}
 @Test fun `login transitions to otp required`()=runTest{val api=Proxy.newProxyInstance(javaClass.classLoader,arrayOf(AuthApi::class.java)){_,m,_->if(m.name=="login")ChallengeDto("challenge",300,30) else null} as AuthApi;val client=DefaultSecureAuthClient(api,MemoryStorage());val result=client.login("a@b.com","password");assertTrue(result is AuthResult.OtpRequired);assertTrue(client.sessionState.value is SessionState.OtpRequired)}
 @Test fun `logout clears local tokens even when network fails`()=runTest{val storage=MemoryStorage().apply{value=StoredTokens("a","r",1)};val api=Proxy.newProxyInstance(javaClass.classLoader,arrayOf(AuthApi::class.java)){_,_,_->throw java.io.IOException()} as AuthApi;val client=DefaultSecureAuthClient(api,storage);client.logout();assertNull(storage.value);assertFalse(client.isAuthenticated())}
}
