package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val MvciDarkColorScheme = darkColorScheme(
    primary = MvciIndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = MvciIndigoDeep,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = MvciAmberAccent,
    onSecondary = Color(0xFF1F1300),
    secondaryContainer = Color(0xFF452800),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = MvciCyanAccent,
    onTertiary = Color(0xFF001F26),
    tertiaryContainer = Color(0xFF083344),
    onTertiaryContainer = Color(0xFFCFFAFE),
    background = MvciNavyDark,
    onBackground = Color(0xFFF1F5F9),
    surface = MvciSlateSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MvciSlateCard,
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLow = MvciSlateSurface,
    surfaceContainer = MvciSlateCard,
    surfaceContainerHigh = MvciSlateElevated,
    outline = MvciBorderDark,
    error = MvciRoseError,
    onError = Color.White
)

private val MvciLightColorScheme = lightColorScheme(
    primary = MvciIndigoDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = MvciIndigoDeep,
    secondary = MvciAmberDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFF0E7490),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFFAFE),
    onTertiaryContainer = Color(0xFF164E63),
    background = MvciLightBg,
    onBackground = MvciLightText,
    surface = MvciLightSurface,
    onSurface = MvciLightText,
    surfaceVariant = MvciLightCard,
    onSurfaceVariant = MvciLightSubtext,
    surfaceContainerLow = MvciLightSurface,
    surfaceContainer = MvciLightCard,
    surfaceContainerHigh = Color(0xFFE2E8F0),
    outline = MvciLightBorder,
    error = Color(0xFFDC2626),
    onError = Color.White
)

val MvciShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MvciControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MvciDarkColorScheme else MvciLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MvciTypography,
        shapes = MvciShapes,
        content = content
    )
}
