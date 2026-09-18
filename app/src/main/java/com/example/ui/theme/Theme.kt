package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AnikaPrimary,
    onPrimary = Color.White,
    primaryContainer = AnikaPrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = AnikaSecondary,
    onSecondary = Color(0xFF0F172A),
    tertiary = AnikaTertiary,
    onTertiary = Color.White,
    background = AnikaDarkBg,
    onBackground = TextPrimary,
    surface = AnikaSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = AnikaCardDark,
    onSurfaceVariant = TextAccent
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
