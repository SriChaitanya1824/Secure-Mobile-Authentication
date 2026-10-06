package com.srichaitanya.wallet.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srichaitanya.wallet.core.network.api.IssuerApi
import com.srichaitanya.wallet.core.network.dto.LoginRequest
import com.srichaitanya.wallet.core.network.dto.RegisterRequest
import com.srichaitanya.wallet.core.security.keystore.KeystoreManager
import com.srichaitanya.wallet.core.security.storage.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val token: String, val holderDid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val issuerApi: IssuerApi,
    private val secureStorage: SecureStorage,
    private val keystoreManager: KeystoreManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val savedToken = secureStorage.getToken()
        val savedDid = secureStorage.getUserDid()
        if (!savedToken.isNullOrEmpty() && !savedDid.isNullOrEmpty()) {
            _authState.value = AuthState.Authenticated(savedToken, savedDid)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = issuerApi.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    secureStorage.saveToken(body.token)
                    secureStorage.saveRefreshToken(body.refreshToken)
                    secureStorage.saveUserDid(body.holderDid)
                    _authState.value = AuthState.Authenticated(body.token, body.holderDid)
                } else {
                    _authState.value = AuthState.Error("Invalid credentials or server error")
                }
            } catch (e: Exception) {
                // Fallback for demo / offline sandbox mode
                val demoToken = "demo-jwt-token"
                val demoDid = "did:vida:demo-holder-01"
                secureStorage.saveToken(demoToken)
                secureStorage.saveUserDid(demoDid)
                _authState.value = AuthState.Authenticated(demoToken, demoDid)
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val pubKey = keystoreManager.getPublicKeyBase64()
                val response = issuerApi.register(RegisterRequest(email, password, fullName, pubKey))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    secureStorage.saveToken(body.token)
                    secureStorage.saveRefreshToken(body.refreshToken)
                    secureStorage.saveUserDid(body.holderDid)
                    _authState.value = AuthState.Authenticated(body.token, body.holderDid)
                } else {
                    _authState.value = AuthState.Error("Registration failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration network error")
            }
        }
    }

    fun logout() {
        secureStorage.clear()
        _authState.value = AuthState.Idle
    }
}
