package com.srichaitanya.wallet.feature.certificate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.srichaitanya.wallet.core.ui.components.QrCodeImage
import com.srichaitanya.wallet.core.ui.components.SecurityStatusBadge
import com.srichaitanya.wallet.core.ui.components.WalletTopBar
import com.srichaitanya.wallet.feature.certificate.CertificateDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateDetailScreen(
    certificateId: String,
    viewModel: CertificateDetailViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(certificateId) {
        viewModel.loadCertificate(certificateId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val cert = uiState.certificate

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Certificate Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (cert == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cert.credentialType.name.replace("_", " "),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    SecurityStatusBadge(status = cert.status)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Holder: ${cert.subjectName}", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("Issuer: ${cert.issuerName}", fontSize = 14.sp)
                        Text("Identifier: ${cert.subjectIdentifier}", fontSize = 14.sp)
                        Text("Algorithm: ${cert.signatureAlgorithm}", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Claims (Selective Disclosure)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select only the claims you wish to share with verifier:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                cert.claims.forEach { claim ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(claim.label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(claim.value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Checkbox(
                            checked = uiState.selectedClaimKeys.contains(claim.key),
                            onCheckedChange = { viewModel.toggleClaimSelection(claim.key) }
                        )
                    }
                    HorizontalDivider()
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.generateQrPresentation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Verification QR Code", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (uiState.showQrDialog && uiState.presentationPayloadJson != null) {
        Dialog(onDismissRequest = { viewModel.dismissQrDialog() }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Verification QR Presentation", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    QrCodeImage(content = uiState.presentationPayloadJson!!, size = 240)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Present this QR to VIDA Verifier Terminal.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.dismissQrDialog() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
