package com.srichaitanya.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.srichaitanya.wallet.core.ui.theme.WalletTheme
import com.srichaitanya.wallet.feature.auth.AuthViewModel
import com.srichaitanya.wallet.feature.auth.ui.LoginScreen
import com.srichaitanya.wallet.feature.certificate.CertificateDetailViewModel
import com.srichaitanya.wallet.feature.certificate.ui.CertificateDetailScreen
import com.srichaitanya.wallet.feature.profile.ProfileViewModel
import com.srichaitanya.wallet.feature.profile.ui.ProfileScreen
import com.srichaitanya.wallet.feature.scanner.ScannerViewModel
import com.srichaitanya.wallet.feature.scanner.ui.QrScannerScreen
import com.srichaitanya.wallet.feature.verification.VerificationViewModel
import com.srichaitanya.wallet.feature.verification.ui.VerificationResultScreen
import com.srichaitanya.wallet.feature.wallet.WalletViewModel
import com.srichaitanya.wallet.feature.wallet.ui.WalletDashboardScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletTheme {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute in listOf("wallet", "profile")) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "wallet",
                                    onClick = { navController.navigate("wallet") },
                                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                                    label = { Text("Wallet") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "profile",
                                    onClick = { navController.navigate("profile") },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                    label = { Text("Profile") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "auth",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("auth") {
                            val authVm: AuthViewModel = hiltViewModel()
                            LoginScreen(
                                viewModel = authVm,
                                onLoginSuccess = {
                                    navController.navigate("wallet") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("wallet") {
                            val walletVm: WalletViewModel = hiltViewModel()
                            WalletDashboardScreen(
                                viewModel = walletVm,
                                onCertificateClick = { certId ->
                                    navController.navigate("certificate/$certId")
                                },
                                onScanQrClick = {
                                    navController.navigate("scanner")
                                }
                            )
                        }

                        composable(
                            route = "certificate/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { backStack ->
                            val id = backStack.arguments?.getString("id") ?: ""
                            val certVm: CertificateDetailViewModel = hiltViewModel()
                            CertificateDetailScreen(
                                certificateId = id,
                                viewModel = certVm,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("scanner") {
                            val scannerVm: ScannerViewModel = hiltViewModel()
                            QrScannerScreen(
                                viewModel = scannerVm,
                                onQrDetected = { payload ->
                                    navController.navigate("verify?payload=$payload")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "verify?payload={payload}",
                            arguments = listOf(navArgument("payload") { type = NavType.StringType; defaultValue = "" })
                        ) { backStack ->
                            val payload = backStack.arguments?.getString("payload") ?: ""
                            val verifyVm: VerificationViewModel = hiltViewModel()
                            VerificationResultScreen(
                                qrPayload = payload,
                                viewModel = verifyVm,
                                onDone = { navController.navigate("wallet") }
                            )
                        }

                        composable("profile") {
                            val profileVm: ProfileViewModel = hiltViewModel()
                            val authVm: AuthViewModel = hiltViewModel()
                            ProfileScreen(
                                viewModel = profileVm,
                                onLogout = {
                                    authVm.logout()
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
