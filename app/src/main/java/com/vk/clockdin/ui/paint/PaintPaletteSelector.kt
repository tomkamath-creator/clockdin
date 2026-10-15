// Location: app/src/main/java/com/vk/clockdin/ui/paint/PaintPaletteSelector.kt
package com.vk.clockdin.ui.paint

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vk.clockdin.domain.model.MoodCategory
import com.vk.clockdin.domain.model.MoodColor
import com.vk.clockdin.ui.theme.ClockdinMotion
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassHighlightSoft
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.GlassSurfaceUltraLow
import com.vk.clockdin.ui.theme.TextDisabled
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.TextTertiary
import com.vk.clockdin.ui.theme.glassCard
import kotlin.math.roundToInt

/**
 * Prominent, high-contrast, bold mood palette selector.
 * Features large 46dp glowing swatches, clear category groupings,
 * active paint indicators, and spring-animated rule feedback.
 */
@Composable
fun PaintPaletteSelector(
    availableColors: List<MoodColor>,
    selectedColors: List<MoodColor>,
    activeColor: MoodColor?,
    isShaking: Boolean,
    validationMessage: String?,
    onColorToggle: (MoodColor) -> Unit,
    onActiveColorSelect: (MoodColor) -> Unit,
    modifier: Modifier = Modifier
) {
    // Spring physics shake offset on rule violation (< 2 or > 5 colors)
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isShaking) {
        if (isShaking) {
            shakeOffset.animateTo(
                targetValue = 18f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    var selectedCategoryFilter by remember { mutableStateOf<MoodCategory?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .padding(vertical = 4.dp)
    ) {
        // 1. Prominent Header with Status Pill and Validation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MOOD PALETTE",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                // Selection Counter Pill
                val countColor = if (selectedColors.size in 2..5) Color(0xFF2FD4A7) else Color(0xFFFFC83D)
                Box(
                    modifier = Modifier
                        .glassCard(shape = RoundedCornerShape(12.dp), fillColor = GlassSurfaceUltraLow)
                        .border(1.dp, countColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${selectedColors.size}/5 selected",
                        color = countColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Validation warning banner
            AnimatedVisibility(
                visible = validationMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                validationMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = Color(0xFFFF5376),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 2. Category Filter Tabs: [All, Positive, Neutral, Negative]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryFilterChip(
                label = "All",
                isSelected = selectedCategoryFilter == null,
                onClick = { selectedCategoryFilter = null }
            )
            CategoryFilterChip(
                label = "☀️ Positive",
                isSelected = selectedCategoryFilter == MoodCategory.POSITIVE,
                onClick = { selectedCategoryFilter = MoodCategory.POSITIVE }
            )
            CategoryFilterChip(
                label = "🌙 Neutral",
                isSelected = selectedCategoryFilter == MoodCategory.NEUTRAL,
                onClick = { selectedCategoryFilter = MoodCategory.NEUTRAL }
            )
            CategoryFilterChip(
                label = "⚡ Negative",
                isSelected = selectedCategoryFilter == MoodCategory.NEGATIVE,
                onClick = { selectedCategoryFilter = MoodCategory.NEGATIVE }
            )
        }

        // 3. Horizontally Scrollable Prominent Swatches
        val filteredColors = remember(availableColors, selectedCategoryFilter) {
            if (selectedCategoryFilter == null) {
                availableColors
            } else {
                availableColors.filter { it.category == selectedCategoryFilter }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            filteredColors.forEach { moodColor ->
                val isSelected = selectedColors.contains(moodColor)
                val isActive = moodColor == activeColor
                val color = parseColor(moodColor.hex)

                ProminentColorSwatch(
                    moodColor = moodColor,
                    color = color,
                    isSelected = isSelected,
                    isActive = isActive,
                    onTap = {
                        if (isSelected) {
                            if (isActive) {
                                // Tapping the already-active color toggles/removes it
                                onColorToggle(moodColor)
                            } else {
                                // Tapping another selected color makes it the active paint color
                                onActiveColorSelect(moodColor)
                            }
                        } else {
                            // Tapping unselected color selects and activates it
                            onColorToggle(moodColor)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Large, eye-catching, luminous circular color swatch.
 */
@Composable
private fun ProminentColorSwatch(
    moodColor: MoodColor,
    color: Color,
    isSelected: Boolean,
    isActive: Boolean,
    onTap: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            )
            .padding(horizontal = 2.dp)
    ) {
        // Swatch Circle with glow and state indicators
        Box(
            modifier = Modifier
                .size(48.dp)
                // Outer glow when active
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isActive) color.copy(alpha = 0.50f) else if (isSelected) color.copy(alpha = 0.20f) else Color.Transparent,
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (isActive) 44.dp else 40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isActive) 3.dp else if (isSelected) 2.dp else 1.dp,
                        color = if (isActive) Color.White else if (isSelected) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.25f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    // Active paint icon inside circle
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Active Paint Color",
                        tint = if (isLightColor(color)) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (isSelected) {
                    // Checkmark indicator for selected colors
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = if (isLightColor(color)) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bold Mood Name
        Text(
            text = moodColor.name,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isActive) TextPrimary else if (isSelected) TextPrimary.copy(alpha = 0.85f) else TextSecondary
        )
    }
}

@Composable
private fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.16f) else GlassSurfaceUltraLow)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.White.copy(alpha = 0.35f) else GlassBorderHairline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) TextPrimary else TextTertiary
        )
    }
}

private fun isLightColor(color: Color): Boolean {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return luminance > 0.6f
}

private fun parseColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val l = clean.toLong(16)
        if (clean.length == 6) Color(l or 0xFF000000) else Color(l)
    } catch (_: Exception) {
        Color(0xFFFFC83D)
    }
}
