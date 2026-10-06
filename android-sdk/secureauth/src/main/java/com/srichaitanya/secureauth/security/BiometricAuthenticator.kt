package com.srichaitanya.secureauth.security
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.srichaitanya.secureauth.api.BiometricResult
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
class BiometricAuthenticator { suspend fun authenticate(activity:FragmentActivity,title:String="Unlock secure session"):BiometricResult=suspendCancellableCoroutine{continuation-> val allowed=BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL; when(BiometricManager.from(activity).canAuthenticate(allowed)){BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED->{continuation.resume(BiometricResult.NotEnrolled);return@suspendCancellableCoroutine};BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE->{continuation.resume(BiometricResult.Unavailable);return@suspendCancellableCoroutine}}
 val prompt=BiometricPrompt(activity,ContextCompat.getMainExecutor(activity),object:BiometricPrompt.AuthenticationCallback(){override fun onAuthenticationSucceeded(r:BiometricPrompt.AuthenticationResult){if(continuation.isActive)continuation.resume(BiometricResult.Success)};override fun onAuthenticationError(code:Int,message:CharSequence){if(!continuation.isActive)return;continuation.resume(when(code){BiometricPrompt.ERROR_USER_CANCELED,BiometricPrompt.ERROR_NEGATIVE_BUTTON->BiometricResult.Cancelled;BiometricPrompt.ERROR_LOCKOUT,BiometricPrompt.ERROR_LOCKOUT_PERMANENT->BiometricResult.LockedOut;else->BiometricResult.Error(message.toString())})}});prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle(title).setAllowedAuthenticators(allowed).build()) }
}
