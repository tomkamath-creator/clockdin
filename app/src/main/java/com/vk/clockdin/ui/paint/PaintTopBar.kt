// Location: app/src/main/java/com/vk/clockdin/ui/paint/PaintTopBar.kt
package com.vk.clockdin.ui.paint

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vk.clockdin.domain.model.MoodColor
import com.vk.clockdin.ui.theme.ClockdinMotion
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.TextTertiary
import com.vk.clockdin.ui.theme.glassCard

@Composable
fun PaintTopBar(
    currentMode: PaintMode,
    canUndo: Boolean,
    canClear: Boolean,
    isRasterizing: Boolean,
    activeColor: MoodColor?,
    onModeChange: (PaintMode) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resolvedAccent = remember(activeColor) {
        try {
            val hex = activeColor?.hex?.removePrefix("#") ?: "FFC83D"
            val colorLong = hex.toLong(16)
            if (hex.length == 6) Color(colorLong or 0xFF000000) else Color(colorLong)
        } catch (_: Exception) {
            Color(0xFFFFC83D)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .glassCard(shape = CircleShape, surfaceAlpha = 0.08f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Mode Switcher (Brush vs Gradient)
        Box(
            modifier = Modifier
                .height(38.dp)
                .glassCard(shape = RoundedCornerShape(20.dp), surfaceAlpha = 0.06f)
                .padding(3.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val isGradient = currentMode == PaintMode.GRADIENT
            val pillOffset by animateDpAsState(
                targetValue = if (isGradient) 74.dp else 0.dp,
                animationSpec = ClockdinMotion.springDefault(),
                label = "ModePillOffset"
            )

            // Animated sliding glass pill background
            Box(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .width(74.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.16f))
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brush Segment
                Row(
                    modifier = Modifier
                        .width(74.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onModeChange(PaintMode.FREEHAND) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = null,
                        tint = if (!isGradient) TextPrimary else TextTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Brush",
                        fontSize = 12.sp,
                        fontWeight = if (!isGradient) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (!isGradient) TextPrimary else TextTertiary
                    )
                }

                // Gradient Segment
                Row(
                    modifier = Modifier
                        .width(74.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onModeChange(PaintMode.GRADIENT) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Gradient,
                        contentDescription = null,
                        tint = if (isGradient) TextPrimary else TextTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Gradient",
                        fontSize = 12.sp,
                        fontWeight = if (isGradient) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isGradient) TextPrimary else TextTertiary
                    )
                }
            }
        }

        // Action Buttons: Undo, Clear, Next
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentMode == PaintMode.FREEHAND) {
                // Undo Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .glassCard(shape = CircleShape, surfaceAlpha = 0.06f)
                        .clickable(
                            enabled = canUndo,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onUndo
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) TextPrimary else TextTertiary.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Clear Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .glassCard(shape = CircleShape, surfaceAlpha = 0.06f)
                        .clickable(
                            enabled = canClear,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClear
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear",
                        tint = if (canClear) TextPrimary else TextTertiary.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Next / Done Button
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .glassCard(
                        shape = RoundedCornerShape(18.dp),
                        surfaceAlpha = 0.15f
                    )
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                resolvedAccent.copy(alpha = 0.30f),
                                resolvedAccent.copy(alpha = 0.10f)
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable(
                        enabled = !isRasterizing,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNext
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRasterizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = TextPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
