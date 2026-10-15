// Location: app/src/main/java/com/vk/clockdin/ui/ribbon/RibbonScreen.kt
package com.vk.clockdin.ui.ribbon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.ui.galaxy.GalaxyFab
import com.vk.clockdin.ui.galaxy.GalaxyPerspective
import com.vk.clockdin.ui.galaxy.GalaxyTopBar
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.GlassSurfaceUltraLow
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.TextTertiary
import com.vk.clockdin.ui.theme.glassCard
import com.vk.clockdin.ui.theme.spaceBackground
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RibbonScreen(
    onNavigateToPerspective: (GalaxyPerspective) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPaint: () -> Unit,
    onEntryClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RibbonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .spaceBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Perspective Top Bar
            GalaxyTopBar(
                streakCount = uiState.streakCount,
                currentPerspective = GalaxyPerspective.RIBBON,
                onPerspectiveSelected = onNavigateToPerspective,
                onSettingsClick = onNavigateToSettings
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TextPrimary, strokeWidth = 2.dp)
                }
            } else if (uiState.entries.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .glassCard(shape = CircleShape, fillColor = GlassSurfaceLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFC83D),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "No Ribbon Timeline Yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "As you paint daily mood orbs, your emotional spectrum ribbon will weave together across time.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .glassCard(shape = RoundedCornerShape(22.dp), fillColor = GlassSurfaceMedium)
                                .clickable { onNavigateToPaint() }
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Paint First Orb ✦",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                // Continuous chromatic ribbon timeline
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    itemsIndexed(uiState.entries, key = { _, entry -> entry.id }) { index, entry ->
                        val nextEntry = uiState.entries.getOrNull(index + 1)
                        RibbonTimelineItem(
                            entry = entry,
                            nextEntry = nextEntry,
                            isLast = index == uiState.entries.size - 1,
                            onClick = { onEntryClick(entry.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating action button
        GalaxyFab(
            onClick = onNavigateToPaint,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }
}

@Composable
private fun RibbonTimelineItem(
    entry: MoodEntry,
    nextEntry: MoodEntry?,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentColor = remember(entry.gradientStops) {
        entry.gradientStops.firstOrNull()?.let { parseColor(it.hex) } ?: Color(0xFFFFC83D)
    }
    val nextColor = remember(nextEntry?.gradientStops) {
        nextEntry?.gradientStops?.firstOrNull()?.let { parseColor(it.hex) } ?: currentColor
    }

    val formattedDate = remember(entry.localDate) {
        try {
            val parsed = LocalDate.parse(entry.localDate)
            parsed.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
        } catch (_: Exception) {
            entry.localDate
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: The Chromatic Ribbon Spine
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(56.dp)
        ) {
            // Ribbon Node Orb
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ambient glow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val glowRadius = size.width / 2f * 1.3f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(currentColor.copy(alpha = 0.50f), Color.Transparent),
                            center = center,
                            radius = glowRadius
                        ),
                        radius = glowRadius,
                        center = center
                    )
                }

                // Orb thumbnail
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, GlassBorderHairline, CircleShape)
                        .background(Color(0xFF07080E)),
                    contentAlignment = Alignment.Center
                ) {
                    if (entry.orbImagePath.isNotBlank()) {
                        AsyncImage(
                            model = File(entry.orbImagePath),
                            contentDescription = "Mood Orb",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Connecting Ribbon Gradient Beam
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(60.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(currentColor, nextColor)
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Right Column: Entry Summary Card
        Box(
            modifier = Modifier
                .weight(1f)
                .glassCard(shape = RoundedCornerShape(18.dp), fillColor = GlassSurfaceLow)
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                // Mood Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    entry.gradientStops.take(3).forEach { stop ->
                        val c = parseColor(stop.hex)
                        Row(
                            modifier = Modifier
                                .glassCard(shape = RoundedCornerShape(8.dp), fillColor = GlassSurfaceUltraLow)
                                .border(0.5.dp, c.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(c)
                            )
                            Text(
                                text = stop.label ?: stop.hex,
                                fontSize = 10.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (!entry.note.isNullOrBlank()) {
                    Text(
                        text = entry.note,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                if (!entry.photoPath.isNullOrBlank()) {
                    val photoFile = File(entry.photoPath)
                    if (photoFile.exists()) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, GlassBorderHairline, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = photoFile,
                                contentDescription = "Attached Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorLong = clean.toLong(16)
        if (clean.length == 6) Color(colorLong or 0xFF000000) else Color(colorLong)
    } catch (_: Exception) {
        Color(0xFFFFC83D)
    }
}
