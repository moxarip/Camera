package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AuraColorScheme = darkColorScheme(
    primary = AuraCyanAccent,
    onPrimary = Color.Black,
    primaryContainer = AuraCyanDim,
    onPrimaryContainer = Color.White,
    secondary = AuraAmberAccent,
    onSecondary = Color.Black,
    secondaryContainer = AuraDarkSurfaceElevated,
    onSecondaryContainer = AuraAmberAccent,
    tertiary = AuraCoralRed,
    onTertiary = Color.White,
    background = AuraDarkBg,
    onBackground = AuraTextPrimary,
    surface = AuraDarkSurface,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraDarkSurfaceElevated,
    onSurfaceVariant = AuraTextSecondary,
    outline = AuraDarkSurfaceBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AuraColorScheme,
        typography = Typography,
        content = content
    )
}
