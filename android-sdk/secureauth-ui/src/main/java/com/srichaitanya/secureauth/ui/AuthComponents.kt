package com.srichaitanya.secureauth.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
@Composable fun LoginForm(loading:Boolean,error:String?,onLogin:(String,String)->Unit,onRegister:()->Unit){var email by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Text("Secure access",style=MaterialTheme.typography.headlineMedium);Text("Authentication protected by short-lived sessions and device security.");OutlinedTextField(email,{email=it},label={Text("Work email")},singleLine=true,modifier=Modifier.fillMaxWidth().semantics{contentDescription="Email address"});OutlinedTextField(password,{password=it},label={Text("Password")},visualTransformation=PasswordVisualTransformation(),singleLine=true,modifier=Modifier.fillMaxWidth().semantics{contentDescription="Password"});error?.let{Text(it,color=MaterialTheme.colorScheme.error)};Button({onLogin(email,password)},enabled=!loading&&email.contains('@')&&password.isNotBlank(),modifier=Modifier.fillMaxWidth()){if(loading)CircularProgressIndicator(Modifier.size(20.dp)) else Text("Continue securely")};TextButton(onRegister,Modifier.fillMaxWidth()){Text("Create account")}}}
@Composable fun OtpForm(challengeId:String,loading:Boolean,onVerify:(String)->Unit){var otp by remember{mutableStateOf("")};Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Text("Verify your identity",style=MaterialTheme.typography.headlineMedium);Text("Enter the six-digit development OTP for challenge ${challengeId.take(8)}…");OutlinedTextField(otp,{otp=it.filter(Char::isDigit).take(6)},label={Text("One-time code")},singleLine=true,modifier=Modifier.fillMaxWidth().semantics{contentDescription="Six digit OTP"});Button({onVerify(otp)},enabled=otp.length==6&&!loading,modifier=Modifier.fillMaxWidth()){Text("Verify")}}}
