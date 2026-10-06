package com.srichaitanya.wallet.feature.scanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srichaitanya.wallet.feature.scanner.ScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    viewModel: ScannerViewModel,
    onQrDetected: (String) -> Unit,
    onBack: () -> Unit
) {
    val scannedPayload by viewModel.scannedPayload.collectAsState()

    LaunchedEffect(scannedPayload) {
        scannedPayload?.let {
            onQrDetected(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Presentation QR", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scanner",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Point camera at digital certificate QR code",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        // Simulation test trigger for emulator / automated testing
                        viewModel.onQrCodeScanned("SIMULATED_QR_PAYLOAD")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simulate Verified Scan")
                }
            }
        }
    }
}
