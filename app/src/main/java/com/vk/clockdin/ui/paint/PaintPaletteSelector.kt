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

import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.window.Dialog

/**
 * Prominent, high-contrast, bold mood palette selector.
 * Features large 46dp glowing swatches, clear category groupings,
 * active paint indicators, custom color creation, and spring-animated rule feedback.
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
    onAddCustomColor: (name: String, hex: String, category: MoodCategory) -> Unit = { _, _, _ -> },
    onDeleteCustomColor: (MoodColor) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddColorDialog by remember { mutableStateOf(false) }
    var colorToDelete by remember { mutableStateOf<MoodColor?>(null) }
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

                // Selection Counter Pill (Unlimited, at least 1)
                val countColor = if (selectedColors.isNotEmpty()) Color(0xFF2FD4A7) else Color(0xFFFFC83D)
                Box(
                    modifier = Modifier
                        .glassCard(shape = RoundedCornerShape(12.dp), fillColor = GlassSurfaceUltraLow)
                        .border(1.dp, countColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${selectedColors.size} selected",
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

        // Dedicated expanding strip of all selected color dots/pills
        if (selectedColors.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                selectedColors.forEach { moodColor ->
                    val isCurrentActive = moodColor == activeColor
                    val color = parseColor(moodColor.hex)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCurrentActive) color.copy(alpha = 0.28f) else GlassSurfaceUltraLow)
                            .border(
                                width = if (isCurrentActive) 1.5.dp else 1.dp,
                                color = if (isCurrentActive) color else color.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onActiveColorSelect(moodColor) }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        )
                        Text(
                            text = moodColor.name,
                            fontSize = 11.sp,
                            color = if (isCurrentActive) TextPrimary else TextSecondary,
                            fontWeight = if (isCurrentActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
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
            // 1. "+ Custom" button at the beginning of swatches row
            AddCustomColorSwatch(
                onClick = { showAddColorDialog = true }
            )

            // 2. Swatches
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
                    },
                    onDelete = if (!moodColor.isDefault) {
                        { colorToDelete = moodColor }
                    } else null
                )
            }
        }

        // Add Custom Color Modal Dialog
        if (showAddColorDialog) {
            AddCustomColorDialog(
                onDismiss = { showAddColorDialog = false },
                onAddColor = { name, hex, cat ->
                    onAddCustomColor(name, hex, cat)
                }
            )
        }

        // Delete Custom Color Confirmation Dialog
        colorToDelete?.let { customColor ->
            DeleteCustomColorDialog(
                color = customColor,
                onDismiss = { colorToDelete = null },
                onConfirm = {
                    onDeleteCustomColor(customColor)
                    colorToDelete = null
                }
            )
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
    onTap: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Box {
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

        // Small remove button for custom colors when selected
        if (onDelete != null && isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xD9E53935))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete custom color",
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

@Composable
private fun AddCustomColorSwatch(
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x33262A36))
                    .border(1.2.dp, Color(0x66FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "+ Custom",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFFD54F)
        )
    }
}

@Composable
private fun DeleteCustomColorDialog(
    color: MoodColor,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF161922))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Delete Custom Color?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Remove \"${color.name}\" from your mood palette?",
                    fontSize = 13.sp,
                    color = Color(0xFFB0B5C6),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(Color(0x26FFFFFF))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(Color(0xFFE53935))
                            .clickable(onClick = onConfirm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
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
