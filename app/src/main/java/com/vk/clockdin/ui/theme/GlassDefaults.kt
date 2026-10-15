// app/src/main/java/com/vk/clockdin/ui/theme/GlassDefaults.kt
package com.vk.clockdin.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Shared constants for the glassmorphic design language.
 * Fake-blur achieved via layered alpha + gradient overlays.
 */
object GlassDefaults {
    /** Card fill: white at ~5% alpha */
    val fillColor = Color(0x0DFFFFFF)
    /** Hairline border: white at ~12% alpha */
    val borderColor = Color(0x1FFFFFFF)
    /** Inner highlight (top edge): white at ~8% alpha */
    val highlightColor = Color(0x14FFFFFF)
    /** Scrim behind modals */
    val scrimColor = Color(0xCC000000)
}