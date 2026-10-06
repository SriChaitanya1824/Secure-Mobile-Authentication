package com.srichaitanya.wallet.feature.wallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srichaitanya.wallet.core.model.CertificateStatus
import com.srichaitanya.wallet.core.ui.components.CertificateCard
import com.srichaitanya.wallet.core.ui.components.WalletTopBar
import com.srichaitanya.wallet.feature.wallet.WalletViewModel

@Composable
fun WalletDashboardScreen(
    viewModel: WalletViewModel,
    onCertificateClick: (String) -> Unit,
    onScanQrClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            WalletTopBar(
                title = "Digital Wallet",
                actions = {
                    IconButton(onClick = onScanQrClick) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search certificates or credentials") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedFilter == null,
                    onClick = { viewModel.onFilterSelected(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = uiState.selectedFilter == CertificateStatus.ACTIVE,
                    onClick = { viewModel.onFilterSelected(CertificateStatus.ACTIVE) },
                    label = { Text("Active") }
                )
                FilterChip(
                    selected = uiState.selectedFilter == CertificateStatus.REVOKED,
                    onClick = { viewModel.onFilterSelected(CertificateStatus.REVOKED) },
                    label = { Text("Revoked") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredCertificates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No certificates found in wallet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.filteredCertificates, key = { it.id }) { cert ->
                        CertificateCard(
                            certificate = cert,
                            onClick = { onCertificateClick(cert.id) }
                        )
                    }
                }
            }
        }
    }
}
