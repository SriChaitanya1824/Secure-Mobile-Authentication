package com.srichaitanya.wallet.feature.verification.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srichaitanya.wallet.core.ui.theme.VidaGreen
import com.srichaitanya.wallet.core.ui.theme.VidaRed
import com.srichaitanya.wallet.feature.verification.VerificationViewModel

@Composable
fun VerificationResultScreen(
    qrPayload: String,
    viewModel: VerificationViewModel,
    onDone: () -> Unit
) {
    LaunchedEffect(qrPayload) {
        viewModel.verifyPresentation(qrPayload)
    }

    val result by viewModel.result.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val r = result!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (r.isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (r.isValid) VidaGreen else VidaRed,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (r.isValid) "Certificate Verified" else "Verification Failed",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                if (r.subjectName != null) {
                    Text(text = "Subject: ${r.subjectName}", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        r.checks.forEach { check ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(check.checkName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (check.passed) "PASSED" else "FAILED",
                                    color = if (check.passed) VidaGreen else VidaRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
