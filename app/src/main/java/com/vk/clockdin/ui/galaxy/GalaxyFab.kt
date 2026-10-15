// Location: app/src/main/java/com/vk/clockdin/ui/galaxy/GalaxyFab.kt
package com.vk.clockdin.ui.galaxy

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vk.clockdin.ui.theme.ClockdinMotion
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassHighlightSoft
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.MoodJoy
import com.vk.clockdin.ui.theme.MoodLoving
import com.vk.clockdin.ui.theme.glassCard

/**
 * Glowing FAB "+" with soft ambient pulsing halo and spring press physics.
 */
@Composable
fun GalaxyFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasEntryToday: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Spring press scale animation
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = ClockdinMotion.springDefault(),
        label = "fab_press_scale"
    )

    // Ambient pulsing glow halo
    val infiniteTransition = rememberInfiniteTransition(label = "fab_halo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val primaryAccent = if (hasEntryToday) MoodLoving else MoodJoy

    Box(
        modifier = modifier
            .scale(pressScale)
            .size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer soft glowing radial halo
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(pulseScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryAccent.copy(alpha = pulseAlpha),
                            primaryAccent.copy(alpha = pulseAlpha * 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Glassmorphic FAB core button
        Box(
            modifier = Modifier
                .size(56.dp)
                .glassCard(
                    shape = CircleShape,
                    fillColor = GlassSurfaceMedium,
                    borderColor = GlassHighlightSoft,
                    borderWidth = 1.5.dp
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Entry",
                tint = primaryAccent,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
