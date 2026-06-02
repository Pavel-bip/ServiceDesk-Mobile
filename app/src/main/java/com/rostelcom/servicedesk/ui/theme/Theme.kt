package com.rostelcom.servicedesk.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple = Color(0xFF9B30FF)
val DarkPurple = Color(0xFF6A0DAD)
val LightPurple = Color(0xFFB388FF)
val BackgroundLight = Color(0xFFF5F0FF)
val BackgroundDark = Color(0xFF1E1E2E)
val SurfaceDark = Color(0xFF2D2D3F)
val TextLight = Color(0xFF4B0082)
val TextDark = Color(0xFFE8E0FF)

private val LightColors = lightColorScheme(
    primary = Purple,
    secondary = LightPurple,
    background = BackgroundLight,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = TextLight,
    onSurface = Color(0xFF2D004D)
)

private val DarkColors = darkColorScheme(
    primary = Purple,
    secondary = LightPurple,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun ServiceDeskTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}