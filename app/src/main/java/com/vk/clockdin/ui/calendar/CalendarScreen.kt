// Location: app/src/main/java/com/vk/clockdin/ui/calendar/CalendarScreen.kt
package com.vk.clockdin.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.style.TextAlign
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
import com.vk.clockdin.ui.galaxy.WeeklyMoodPercentageCard
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.GlassSurfaceUltraLow
import com.vk.clockdin.ui.theme.MoodJoy
import com.vk.clockdin.ui.theme.TextDisabled
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.TextTertiary
import com.vk.clockdin.ui.theme.glassCard
import com.vk.clockdin.ui.theme.spaceBackground
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    onNavigateToPerspective: (GalaxyPerspective) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPaint: () -> Unit,
    onEntryClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Perspective Top Bar
            GalaxyTopBar(
                streakCount = uiState.streakCount,
                currentPerspective = GalaxyPerspective.CALENDAR,
                onPerspectiveSelected = onNavigateToPerspective,
                onSettingsClick = onNavigateToSettings
            )

            // Weekly Mood Breakdown Card
            if (uiState.weeklyStats.totalEntriesThisWeek > 0) {
                WeeklyMoodPercentageCard(
                    stats = uiState.weeklyStats,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Month Navigation Card
            MonthHeader(
                currentYearMonth = uiState.currentYearMonth,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Calendar Grid Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .glassCard(shape = RoundedCornerShape(24.dp), fillColor = Color(0xD9161922))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                    .padding(14.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Day of week headers (Sun .. Sat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 7-Column Days Grid
                    val firstDayOffset = uiState.currentYearMonth.atDay(1).dayOfWeek.value % 7
                    val daysInMonth = uiState.currentYearMonth.lengthOfMonth()
                    val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in 0 until (totalCells / 7)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNumber = cellIndex - firstDayOffset + 1

                                    if (dayNumber in 1..daysInMonth) {
                                        val date = uiState.currentYearMonth.atDay(dayNumber)
                                        val entriesForDay = uiState.entriesByDate[date.toString()] ?: emptyList()
                                        val isSelected = date == uiState.selectedDate
                                        val isToday = date == LocalDate.now()

                                        CalendarDayCell(
                                            dayNumber = dayNumber,
                                            entries = entriesForDay,
                                            isSelected = isSelected,
                                            isToday = isToday,
                                            onClick = { viewModel.selectDate(date) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        // Empty cell outside current month
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selected Day Orbs Section (Displays ALL entries recorded on that date!)
            SelectedDayEntriesSection(
                selectedDate = uiState.selectedDate,
                entries = uiState.selectedEntries,
                onViewEntry = { entryId -> onEntryClick(entryId) },
                onPaintOrb = onNavigateToPaint,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(84.dp))
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
private fun MonthHeader(
    currentYearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthText = remember(currentYearMonth) {
        currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(20.dp), fillColor = Color(0xD9161922))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0x33262A36))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPreviousMonth
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Month",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = monthText,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0x33262A36))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNextMonth
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Month",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    entries: List<MoodEntry>,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color.White.copy(alpha = 0.16f) else Color.Transparent
            )
            .border(
                width = if (isSelected) 1.dp else if (isToday) 1.dp else 0.dp,
                color = if (isSelected) Color.White.copy(alpha = 0.60f) else if (isToday) MoodJoy.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                fontSize = 12.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) TextPrimary else if (isToday) MoodJoy else TextSecondary
            )

            if (entries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                // Radiant mini orb dot(s) for multiple entries per day
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayed = entries.take(3)
                    displayed.forEach { entry ->
                        val dotColor = remember(entry.gradientStops) {
                            entry.gradientStops.firstOrNull()?.let { parseColor(it.hex) } ?: Color(0xFFFFC83D)
                        }
                        Box(
                            modifier = Modifier
                                .size(if (displayed.size == 1) 7.dp else 5.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(0.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(7.dp))
            }
        }
    }
}

@Composable
private fun SelectedDayEntriesSection(
    selectedDate: LocalDate,
    entries: List<MoodEntry>,
    onViewEntry: (Long) -> Unit,
    onPaintOrb: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedDate,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (entries.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33161922))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${entries.size} ${if (entries.size == 1) "Orb" else "Orbs"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2FD4A7)
                    )
                }
            }
        }

        if (entries.isNotEmpty()) {
            // Render each mood entry card recorded on that day
            entries.forEach { entry ->
                val primaryColor = remember(entry.gradientStops) {
                    entry.gradientStops.firstOrNull()?.let { parseColor(it.hex) } ?: Color(0xFFFFC83D)
                }
                val timeStr = remember(entry.createdAt) {
                    try {
                        java.time.Instant.ofEpochMilli(entry.createdAt)
                            .atZone(java.time.ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
                    } catch (_: Exception) {
                        "Recorded"
                    }
                }
                val photoCount = remember(entry.photoPath) {
                    entry.photoPaths.filter { it.isNotBlank() }.size
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(shape = RoundedCornerShape(20.dp), fillColor = Color(0xD9161922))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Orb preview image with subtle ambient glow
                            Box(
                                modifier = Modifier.size(54.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(primaryColor.copy(alpha = 0.45f), Color.Transparent),
                                            center = center,
                                            radius = size.width / 2f * 1.2f
                                        ),
                                        radius = size.width / 2f * 1.2f,
                                        center = center
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, GlassBorderHairline, CircleShape)
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

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = timeStr,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFB0B5C6)
                                    )

                                    if (photoCount > 0) {
                                        Text(
                                            text = "📷 $photoCount ${if (photoCount == 1) "photo" else "photos"}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8A90A5)
                                        )
                                    }
                                }

                                // Mood Pills
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    entry.gradientStops.take(4).forEach { stop ->
                                        val c = parseColor(stop.hex)
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x66161922))
                                                .border(0.5.dp, c.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
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
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                if (!entry.note.isNullOrBlank()) {
                                    Text(
                                        text = entry.note,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE2E4EB),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // View Details Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0x33262A36))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp))
                                .clickable { onViewEntry(entry.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "View Orb Details ✦",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Quick Add Another Orb on this day
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color(0x26161922))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(21.dp))
                    .clickable { onPaintOrb() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Paint Another Mood Orb",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFC83D)
                )
            }
        } else {
            // No entries on this day
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(20.dp), fillColor = Color(0xD9161922))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "No mood orb recorded for this day.",
                        fontSize = 13.sp,
                        color = Color(0xFFB0B5C6)
                    )

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF7C4DFF).copy(alpha = 0.5f), Color(0xFF00E5FF).copy(alpha = 0.35f))
                                )
                            )
                            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(20.dp))
                            .clickable { onPaintOrb() }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Paint Today's Orb ✦",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
        val colorLong = clean.toLong(16)
        if (clean.length == 6) Color(colorLong or 0xFF000000) else Color(colorLong)
    } catch (_: Exception) {
        Color(0xFFFFC83D)
    }
}
