package com.ekora.chat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EkoraLightColorScheme = lightColorScheme(
    primary = Color(0xFF1B7A43),            // vert EkoraChat
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB9F0CD),   // bulles de MES messages
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF4E6355),
    onSecondary = Color.White,
    background = Color(0xFFF6FBF4),
    onBackground = Color(0xFF171D19),
    surface = Color(0xFFF6FBF4),
    onSurface = Color(0xFF171D19),
    surfaceVariant = Color(0xFFDDE5DB),     // bulles des messages reçus
    onSurfaceVariant = Color(0xFF414942),
    outline = Color(0xFF717970),            // bordures des OutlinedTextField
    error = Color(0xFFBA1A1A),
)

@Composable
fun EkoraChatTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = EkoraLightColorScheme,
        typography = Typography,
        content = content,
    )
}