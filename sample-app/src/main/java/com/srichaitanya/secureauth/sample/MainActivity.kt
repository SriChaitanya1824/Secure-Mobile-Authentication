package com.srichaitanya.secureauth.sample
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.srichaitanya.secureauth.api.*
import com.srichaitanya.secureauth.ui.*
import kotlinx.coroutines.launch
class MainActivity:ComponentActivity(){ private lateinit var auth:SecureAuthClient; override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);auth=SecureAuth.initialize(this,SecureAuthConfiguration("http://10.0.2.2:8080/"));setContent{MaterialTheme(colorScheme=lightColorScheme(primary=androidx.compose.ui.graphics.Color(0xFF006C4C))){val state by auth.sessionState.collectAsState();Surface(Modifier.fillMaxSize()){when(val s=state){is SessionState.Unauthenticated,is SessionState.Error->LoginForm(false,(s as? SessionState.Error)?.let{"Authentication failed. Please retry."},{e,p->lifecycleScope.launch{auth.login(e,p)}},{ });is SessionState.Authenticating->Box(Modifier.padding(32.dp)){CircularProgressIndicator()};is SessionState.OtpRequired->OtpForm(s.challengeId,false){otp->lifecycleScope.launch{auth.verifyOtp(s.challengeId,otp)}};is SessionState.Authenticated->Home(s.user){lifecycleScope.launch{auth.logout()}};is SessionState.Refreshing->Box(Modifier.padding(32.dp)){Text("Refreshing secure session…")};is SessionState.Expired->LoginForm(false,"Session expired",{e,p->lifecycleScope.launch{auth.login(e,p)}},{});is SessionState.Locked->Box(Modifier.padding(32.dp)){Text("Account locked")}}}}}}
}
@Composable private fun Home(user:AuthUser?,logout:()->Unit){Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Text("Security Console",style=MaterialTheme.typography.headlineMedium);Text("Authenticated session active");Text(user?.displayName?:"Profile available after sync");Card{Column(Modifier.padding(16.dp)){Text("Session protection");Text("Keystore encrypted • Refresh rotation • 5 minute access token")}};Button(logout){Text("Sign out securely")}}}
