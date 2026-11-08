package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

val HackerColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = CyberBlack,
    primaryContainer = NeonGreenDark,
    onPrimaryContainer = TextBright,
    secondary = NeonCyan,
    onSecondary = CyberBlack,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = NeonCyan,
    tertiary = NeonPink,
    onTertiary = CyberBlack,
    background = CyberBlack,
    onBackground = TextBright,
    surface = CyberDark,
    onSurface = TextBright,
    surfaceVariant = CyberSurface,
    onSurfaceVariant = TextDim,
    outline = CyberCardBorder,
    error = ErrorRed,
    onError = CyberBlack
)

@Composable
fun AnmolNotesTheme(
    fontFamily: FontFamily = FiraCodeFontFamily,
    fontSizeScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HackerColorScheme,
        typography = getHackerTypography(fontFamily, fontSizeScale),
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AnmolNotesTheme(content = content)
}
