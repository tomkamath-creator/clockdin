// app/src/main/java/com/vk/clockdin/ui/theme/ClockdinTheme.kt
package com.vk.clockdin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** All UI chrome is dark neutral; saturated colors appear only in mood orbs. */
private val DarkColors = darkColorScheme(
    primary          = Color(0xFFE8E8F0),
    onPrimary        = Color(0xFF0A0A12),
    primaryContainer = Color(0xFF1C1C2A),
    secondary        = Color(0xFFB0B0C8),
    onSecondary      = Color(0xFF0A0A12),
    background       = Color(0xFF090A0F),
    onBackground     = Color(0xFFEEEEF5),
    surface          = Color(0xFF101018),
    onSurface        = Color(0xFFEEEEF5),
    surfaceVariant   = Color(0xFF1A1A26),
    onSurfaceVariant = Color(0xFFB0B0C0),
    outline          = Color(0xFF2A2A3A),
    error            = Color(0xFFCF6679),
    onError          = Color(0xFF690020),
)

@Composable
fun ClockdinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = ClockdinTypography,
        content = content
    )
}