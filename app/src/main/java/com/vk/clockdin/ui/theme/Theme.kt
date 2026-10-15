// Location: app/src/main/java/com/vk/clockdin/ui/theme/Theme.kt
package com.vk.clockdin.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spring physics motion constants for Clockdin.
 * Mandatory design rule: Every interaction uses spring physics.
 * No linear or tween for user interactions.
 */
object ClockdinMotion {
    fun <T> springDefault(): AnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    fun <T> springBouncy(): AnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessLow
    )

    fun <T> springSnappy(): AnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = TextPrimary,
    onPrimary = PureBlack,
    secondary = TextSecondary,
    onSecondary = PureBlack,
    tertiary = TextTertiary,
    background = CosmicAbyss,
    onBackground = TextPrimary,
    surface = GlassSurfaceLow,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceUltraLow,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderHairline
)

/**
 * Applies the ultra-premium vertical dark gradient: #090A0F -> #000000
 */
fun Modifier.spaceBackground(): Modifier = this.background(
    brush = Brush.verticalGradient(
        colors = listOf(CosmicAbyss, PureBlack)
    )
)

/**
 * Glassmorphic card styling:
 * Translucent white (4-8% alpha) fill, 1dp hairline border (white ~12% alpha),
 * soft inner highlight, rounded 24dp.
 */
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(24.dp),
    fillColor: Color = GlassSurfaceLow,
    borderColor: Color = GlassBorderHairline,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                fillColor.copy(alpha = fillColor.alpha * 1.35f),
                fillColor.copy(alpha = fillColor.alpha * 0.7f)
            )
        ),
        shape = shape
    )
    .border(
        width = borderWidth,
        brush = Brush.verticalGradient(
            colors = listOf(
                GlassHighlightSoft,
                borderColor,
                borderColor.copy(alpha = borderColor.alpha * 0.4f)
            )
        ),
        shape = shape
    )

@Composable
fun ClockdinTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
