package com.srichaitanya.wallet.feature.scanner

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {
    private val _scannedPayload = MutableStateFlow<String?>(null)
    val scannedPayload: StateFlow<String?> = _scannedPayload.asStateFlow()

    fun onQrCodeScanned(qrContent: String) {
        if (_scannedPayload.value == null) {
            _scannedPayload.value = qrContent
        }
    }

    fun resetScanner() {
        _scannedPayload.value = null
    }
}
