package com.sangyoon.parkingpass.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = TextPrimary,

    secondary = SecondaryPurple,
    onSecondary = TextPrimary,
    secondaryContainer = SecondaryPurple,
    onSecondaryContainer = TextPrimary,

    tertiary = StatusInfo,
    onTertiary = TextPrimary,

    background = DarkBackground,
    onBackground = TextPrimary,

    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,

    error = StatusError,
    onError = TextPrimary,
    errorContainer = StatusError,
    onErrorContainer = TextPrimary,

    outline = BorderColor,
    outlineVariant = DividerColor,

    scrim = DarkOverlay
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),  // Card default
    large = RoundedCornerShape(24.dp),   // Button default
    extraLarge = RoundedCornerShape(28.dp) // SearchBar
)

@Composable
fun ParkingPassTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}
