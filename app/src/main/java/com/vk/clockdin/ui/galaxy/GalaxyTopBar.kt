// Location: app/src/main/java/com/vk/clockdin/ui/galaxy/GalaxyTopBar.kt
package com.vk.clockdin.ui.galaxy

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vk.clockdin.ui.navigation.Screen
import com.vk.clockdin.ui.theme.ClockdinMotion
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.MoodJoy
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.glassCard

enum class GalaxyPerspective(val label: String, val route: String) {
    GALAXY("Galaxy", Screen.Galaxy.route),
    LIST("List", Screen.List.route),
    CALENDAR("Calendar", Screen.Calendar.route),
    RIBBON("Ribbon", Screen.Ribbon.route)
}

@Composable
fun GalaxyTopBar(
    streakCount: Int,
    currentPerspective: GalaxyPerspective,
    onPerspectiveSelected: (GalaxyPerspective) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Streak Glass Pill
        StreakBadge(streakCount = streakCount)

        // Perspective Switcher Glass Pill
        PerspectiveSwitcher(
            currentPerspective = currentPerspective,
            onPerspectiveSelected = onPerspectiveSelected
        )

        // Settings Button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(40.dp)
                .glassCard(
                    shape = CircleShape,
                    fillColor = GlassSurfaceLow,
                    borderColor = GlassBorderHairline
                )
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = TextPrimary.copy(alpha = 0.85f),
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun StreakBadge(streakCount: Int) {
    val text = if (streakCount > 0) "🔥 $streakCount days" else "✨ Begin"
    Box(
        modifier = Modifier
            .glassCard(
                shape = RoundedCornerShape(20.dp),
                fillColor = GlassSurfaceLow,
                borderColor = if (streakCount > 0) MoodJoy.copy(alpha = 0.35f) else GlassBorderHairline
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (streakCount > 0) MoodJoy else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
private fun PerspectiveSwitcher(
    currentPerspective: GalaxyPerspective,
    onPerspectiveSelected: (GalaxyPerspective) -> Unit
) {
    Box(
        modifier = Modifier
            .glassCard(
                shape = RoundedCornerShape(24.dp),
                fillColor = GlassSurfaceLow,
                borderColor = GlassBorderHairline
            )
            .padding(3.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GalaxyPerspective.entries.forEach { perspective ->
                val isSelected = perspective == currentPerspective
                val targetBackground = if (isSelected) GlassSurfaceMedium else Color.Transparent
                val animatedBackground by animateColorAsState(
                    targetValue = targetBackground,
                    animationSpec = ClockdinMotion.springDefault(),
                    label = "perspective_tab_bg"
                )

                Box(
                    modifier = Modifier
                        .glassCard(
                            shape = RoundedCornerShape(20.dp),
                            fillColor = animatedBackground,
                            borderColor = if (isSelected) GlassBorderHairline else Color.Transparent,
                            borderWidth = 1.dp
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                onPerspectiveSelected(perspective)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = perspective.label,
                        color = if (isSelected) TextPrimary else TextSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }
    }
}
