package com.srichaitanya.secureauth.storage
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
data class StoredTokens(val access:String,val refresh:String,val expiresAtEpochSeconds:Long)
interface TokenStorage { fun read():StoredTokens?; fun write(tokens:StoredTokens); fun clear() }
class KeystoreTokenStorage(context:Context):TokenStorage { private val prefs=context.getSharedPreferences("secureauth_encrypted",Context.MODE_PRIVATE); private val alias="secureauth.token.key.v1"
 private fun key():java.security.Key { val ks=KeyStore.getInstance("AndroidKeyStore").apply{load(null)}; return ks.getKey(alias,null)?:KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").run{init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());generateKey()} }
 override fun write(tokens:StoredTokens){ val c=Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE,key()); val plain="${tokens.access}\n${tokens.refresh}\n${tokens.expiresAtEpochSeconds}".toByteArray(); prefs.edit().putString("data",Base64.encodeToString(c.doFinal(plain),Base64.NO_WRAP)).putString("iv",Base64.encodeToString(c.iv,Base64.NO_WRAP)).apply() }
 override fun read():StoredTokens?=try{ val data=Base64.decode(prefs.getString("data",null),Base64.NO_WRAP); val iv=Base64.decode(prefs.getString("iv",null),Base64.NO_WRAP); val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,iv));val p=String(c.doFinal(data)).split('\n');StoredTokens(p[0],p[1],p[2].toLong()) }catch(_:Exception){ clear();null }
 override fun clear(){prefs.edit().clear().apply(); runCatching{KeyStore.getInstance("AndroidKeyStore").apply{load(null);deleteEntry(alias)}}}
}
