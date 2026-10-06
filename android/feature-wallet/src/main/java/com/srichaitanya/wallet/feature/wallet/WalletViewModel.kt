package com.srichaitanya.wallet.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srichaitanya.wallet.core.model.Certificate
import com.srichaitanya.wallet.core.model.CertificateStatus
import com.srichaitanya.wallet.core.model.CredentialType
import com.srichaitanya.wallet.core.usecase.CertificateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val certificates: List<Certificate> = emptyList(),
    val filteredCertificates: List<Certificate> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: CertificateStatus? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: CertificateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadCertificates()
    }

    private fun loadCertificates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getCertificates().collect { list ->
                _uiState.update { state ->
                    state.copy(
                        certificates = list,
                        filteredCertificates = filterList(list, state.searchQuery, state.selectedFilter),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredCertificates = filterList(state.certificates, query, state.selectedFilter)
            )
        }
    }

    fun onFilterSelected(status: CertificateStatus?) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = status,
                filteredCertificates = filterList(state.certificates, state.searchQuery, status)
            )
        }
    }

    fun refreshCertificates(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.syncWithIssuer(token)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun filterList(
        list: List<Certificate>,
        query: String,
        status: CertificateStatus?
    ): List<Certificate> {
        return list.filter { cert ->
            val matchesQuery = query.isBlank() ||
                    cert.subjectName.contains(query, ignoreCase = true) ||
                    cert.credentialType.name.contains(query, ignoreCase = true) ||
                    cert.issuerName.contains(query, ignoreCase = true)
            val matchesStatus = status == null || cert.status == status
            matchesQuery && matchesStatus
        }
    }
}
