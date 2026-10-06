package com.srichaitanya.wallet.feature.profile

import androidx.lifecycle.ViewModel
import com.srichaitanya.wallet.core.model.DeviceSecurityScore
import com.srichaitanya.wallet.core.security.biometric.BiometricAuthManager
import com.srichaitanya.wallet.core.security.keystore.KeystoreManager
import com.srichaitanya.wallet.core.usecase.AssessDeviceSecurityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val keystoreManager: KeystoreManager,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val assessUseCase = AssessDeviceSecurityUseCase()
    private val _securityScore = MutableStateFlow<DeviceSecurityScore?>(null)
    val securityScore: StateFlow<DeviceSecurityScore?> = _securityScore.asStateFlow()

    init {
        val score = assessUseCase(
            isHardwareBacked = keystoreManager.isHardwareBacked(),
            isBiometricsAvailable = biometricAuthManager.isBiometricAvailable(),
            isScreenLockSet = true,
            isRooted = false
        )
        _securityScore.value = score
    }
}
