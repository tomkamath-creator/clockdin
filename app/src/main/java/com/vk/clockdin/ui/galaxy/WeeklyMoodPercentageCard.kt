// Location: app/src/main/java/com/vk/clockdin/ui/galaxy/WeeklyMoodPercentageCard.kt
package com.vk.clockdin.ui.galaxy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vk.clockdin.domain.model.WeeklyMoodStats
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceUltraLow
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.TextTertiary
import com.vk.clockdin.ui.theme.glassCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyMoodPercentageCard(
    stats: WeeklyMoodStats,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false
) {
    if (stats.totalEntriesThisWeek <= 0 || stats.majorityMood == null) {
        return
    }

    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    val majorityColor = remember(stats.majorityHexColor) {
        parseColor(stats.majorityHexColor)
    }

    Box(
        modifier = modifier
            .animateContentSize()
            .glassCard(
                shape = RoundedCornerShape(20.dp),
                fillColor = GlassSurfaceLow.copy(alpha = 0.85f),
                borderColor = majorityColor.copy(alpha = 0.35f),
                borderWidth = 1.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { isExpanded = !isExpanded }
            )
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Summary header pill row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(majorityColor)
                    )

                    Text(
                        text = "Majority Mood: ",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${stats.majorityMood} (${stats.majorityPercentage}%)",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Expanded Breakdown Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Weekly Breakdown (${stats.totalEntriesThisWeek} ${if (stats.totalEntriesThisWeek == 1) "entry" else "entries"})",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        fontWeight = FontWeight.Medium
                    )

                    // Multi-colored proportional segmented bar
                    if (stats.breakdown.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            stats.breakdown.forEach { item ->
                                val itemColor = parseColor(item.hexColor)
                                Box(
                                    modifier = Modifier
                                        .weight(item.percentage.coerceAtLeast(1).toFloat())
                                        .height(8.dp)
                                        .background(itemColor)
                                )
                            }
                        }
                    }

                    // FlowRow of mood percentage pills
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        stats.breakdown.forEach { item ->
                            val itemColor = parseColor(item.hexColor)
                            Row(
                                modifier = Modifier
                                    .glassCard(
                                        shape = RoundedCornerShape(12.dp),
                                        fillColor = GlassSurfaceUltraLow,
                                        borderColor = itemColor.copy(alpha = 0.4f),
                                        borderWidth = 1.dp
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(itemColor)
                                )

                                Text(
                                    text = "${item.label} ${item.percentage}%",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return Color(0xFFFFC83D)
    return try {
        val clean = hex.removePrefix("#")
        val l = clean.toLong(16)
        if (clean.length == 6) Color(l or 0xFF000000) else Color(l)
    } catch (_: Exception) {
        Color(0xFFFFC83D)
    }
}
