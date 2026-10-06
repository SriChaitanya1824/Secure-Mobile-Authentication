package com.srichaitanya.wallet.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VidaNavy = Color(0xFF0F172A)
val VidaCyan = Color(0xFF0EA5E9)
val VidaGreen = Color(0xFF10B981)
val VidaAmber = Color(0xFFF59E0B)
val VidaRed = Color(0xFFEF4444)
val VidaCardDark = Color(0xFF1E293B)
val VidaCardLight = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = VidaCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0369A1),
    secondary = VidaGreen,
    onSecondary = Color.Black,
    background = Color(0xFF0B0F17),
    surface = Color(0xFF131B2A),
    surfaceVariant = VidaCardDark,
    error = VidaRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    error = VidaRed
)

@Composable
fun WalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
