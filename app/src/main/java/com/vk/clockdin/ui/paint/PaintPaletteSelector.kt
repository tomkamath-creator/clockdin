// Location: app/src/main/java/com/vk/clockdin/ui/paint/PaintPaletteSelector.kt
package com.vk.clockdin.ui.paint

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vk.clockdin.domain.model.MoodCategory
import com.vk.clockdin.domain.model.MoodColor
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassHighlightSoft
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.TextTertiary
import com.vk.clockdin.ui.theme.glassCard
import kotlin.math.roundToInt

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
    // Spring physics shake offset on rule violation
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .padding(vertical = 8.dp)
    ) {
        // Stack of currently selected glowing dots + Validation Message
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selected dots indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected (${selectedColors.size}/5):",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 0.3.sp
                )
                selectedColors.forEach { moodColor ->
                    val c = parseColor(moodColor.hex)
                    val isActive = moodColor == activeColor
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 16.dp else 12.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) Color.White else GlassBorderHairline,
                                shape = CircleShape
                            )
                            .clickable { onActiveColorSelect(moodColor) }
                    )
                }
            }

            if (validationMessage != null) {
                Text(
                    text = validationMessage,
                    color = Color(0xFFFF5376),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontally scrolling categories: Positive, Neutral, Negative
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val grouped = availableColors.groupBy { it.category }

            listOf(MoodCategory.POSITIVE, MoodCategory.NEUTRAL, MoodCategory.NEGATIVE).forEach { cat ->
                val colors = grouped[cat] ?: emptyList()
                if (colors.isNotEmpty()) {
                    PaletteCategoryGroup(
                        title = when (cat) {
                            MoodCategory.POSITIVE -> "Positive"
                            MoodCategory.NEUTRAL -> "Neutral"
                            MoodCategory.NEGATIVE -> "Negative"
                        },
                        colors = colors,
                        selectedColors = selectedColors,
                        activeColor = activeColor,
                        onColorClick = onColorToggle,
                        onActiveClick = onActiveColorSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun PaletteCategoryGroup(
    title: String,
    colors: List<MoodColor>,
    selectedColors: List<MoodColor>,
    activeColor: MoodColor?,
    onColorClick: (MoodColor) -> Unit,
    onActiveClick: (MoodColor) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEach { moodColor ->
                val isSelected = selectedColors.contains(moodColor)
                val isActive = moodColor == activeColor
                val color = parseColor(moodColor.hex)

                Box(
                    modifier = Modifier
                        .glassCard(
                            shape = RoundedCornerShape(20.dp),
                            fillColor = if (isActive) color.copy(alpha = 0.35f) else GlassSurfaceLow,
                            borderColor = if (isActive) color else if (isSelected) GlassHighlightSoft else GlassBorderHairline,
                            borderWidth = if (isActive) 1.5.dp else 1.dp
                        )
                        .clickable {
                            if (isSelected) {
                                onActiveClick(moodColor)
                            } else {
                                onColorClick(moodColor)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored glowing dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Text(
                            text = moodColor.name,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
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
