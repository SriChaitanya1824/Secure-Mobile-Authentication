package com.srichaitanya.wallet.feature.certificate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srichaitanya.wallet.core.model.Certificate
import com.srichaitanya.wallet.core.model.PresentationPayload
import com.srichaitanya.wallet.core.security.keystore.KeystoreManager
import com.srichaitanya.wallet.core.usecase.CertificateRepository
import com.srichaitanya.wallet.core.usecase.GeneratePresentationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

data class CertificateDetailUiState(
    val certificate: Certificate? = null,
    val selectedClaimKeys: Set<String> = emptySet(),
    val presentationPayloadJson: String? = null,
    val showQrDialog: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class CertificateDetailViewModel @Inject constructor(
    private val repository: CertificateRepository,
    private val keystoreManager: KeystoreManager
) : ViewModel() {

    private val presentationUseCase = GeneratePresentationUseCase()
    private val _uiState = MutableStateFlow(CertificateDetailUiState())
    val uiState: StateFlow<CertificateDetailUiState> = _uiState.asStateFlow()

    fun loadCertificate(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val cert = repository.getCertificateById(id)
            if (cert != null) {
                _uiState.update {
                    it.copy(
                        certificate = cert,
                        selectedClaimKeys = cert.claims.map { c -> c.key }.toSet(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleClaimSelection(key: String) {
        _uiState.update { state ->
            val updated = state.selectedClaimKeys.toMutableSet()
            if (updated.contains(key)) updated.remove(key) else updated.add(key)
            state.copy(selectedClaimKeys = updated)
        }
    }

    fun generateQrPresentation() {
        val cert = _uiState.value.certificate ?: return
        val nonce = UUID.randomUUID().toString()
        val holderSig = keystoreManager.signString("presentation-nonce:$nonce")
        val presentation = presentationUseCase(
            certificate = cert,
            selectedClaimKeys = _uiState.value.selectedClaimKeys,
            nonce = nonce,
            holderSignature = holderSig
        )
        val json = Json.encodeToString(PresentationPayload.serializer(), presentation)
        _uiState.update { it.copy(presentationPayloadJson = json, showQrDialog = true) }
    }

    fun dismissQrDialog() {
        _uiState.update { it.copy(showQrDialog = false) }
    }
}
