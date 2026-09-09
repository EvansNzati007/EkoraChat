package com.ekora.chat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

private val EkoraDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9DD4AF),            // vert EkoraChat, éclairci pour le sombre
    onPrimary = Color(0xFF00391C),
    primaryContainer = Color(0xFF0F5132),   // bulles de MES messages
    onPrimaryContainer = Color(0xFFB9F0CD),
    secondary = Color(0xFFB5CCBE),
    onSecondary = Color(0xFF213528),
    background = Color(0xFF10140F),
    onBackground = Color(0xFFDFE4DA),
    surface = Color(0xFF10140F),
    onSurface = Color(0xFFDFE4DA),
    surfaceVariant = Color(0xFF2B322D),      // bulles des messages reçus
    onSurfaceVariant = Color(0xFFC1C9BE),
    outline = Color(0xFF8B938A),             // bordures des OutlinedTextField
    error = Color(0xFFFFB4AB),
)

@Composable
fun EkoraChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EkoraDarkColorScheme else EkoraLightColorScheme,
        typography = Typography,
        content = content,
    )
}