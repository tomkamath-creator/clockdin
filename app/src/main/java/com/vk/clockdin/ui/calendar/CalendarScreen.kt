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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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
                    .glassCard(shape = RoundedCornerShape(24.dp), fillColor = GlassSurfaceLow)
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
                                        val entry = uiState.entriesByDate[date.toString()]
                                        val isSelected = date == uiState.selectedDate
                                        val isToday = date == LocalDate.now()

                                        CalendarDayCell(
                                            dayNumber = dayNumber,
                                            entry = entry,
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

            // Selected Day Orb Summary Card
            SelectedDaySummaryCard(
                selectedDate = uiState.selectedDate,
                entry = uiState.selectedEntry,
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
            .glassCard(shape = RoundedCornerShape(20.dp), fillColor = GlassSurfaceUltraLow)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .glassCard(shape = CircleShape, fillColor = GlassSurfaceLow)
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
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = 0.3.sp
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .glassCard(shape = CircleShape, fillColor = GlassSurfaceLow)
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
    entry: MoodEntry?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = remember(entry?.gradientStops) {
        entry?.gradientStops?.firstOrNull()?.let { parseColor(it.hex) } ?: Color(0xFFFFC83D)
    }

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

            if (entry != null) {
                Spacer(modifier = Modifier.height(2.dp))
                // Radiant mini orb dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                        .border(0.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SelectedDaySummaryCard(
    selectedDate: LocalDate,
    entry: MoodEntry?,
    onViewEntry: (Long) -> Unit,
    onPaintOrb: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(22.dp), fillColor = GlassSurfaceLow)
            .padding(16.dp)
    ) {
        if (entry != null) {
            val primaryColor = remember(entry.gradientStops) {
                entry.gradientStops.firstOrNull()?.let { parseColor(it.hex) } ?: Color(0xFFFFC83D)
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Orb Image with ambient glow
                    Box(
                        modifier = Modifier.size(58.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.50f), Color.Transparent),
                                    center = center,
                                    radius = size.width / 2f * 1.25f
                                ),
                                radius = size.width / 2f * 1.25f,
                                center = center
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
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
                        Text(
                            text = formattedDate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        // Mood Pills
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
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // View Details Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .glassCard(shape = RoundedCornerShape(19.dp), fillColor = GlassSurfaceMedium)
                        .clickable { onViewEntry(entry.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "View Orb Details ✦",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        } else {
            // No entry on this day
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = "No mood orb recorded for this day.",
                    fontSize = 13.sp,
                    color = TextTertiary
                )

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .glassCard(shape = RoundedCornerShape(19.dp), fillColor = GlassSurfaceMedium)
                        .clickable { onPaintOrb() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Paint Today's Orb ✦",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
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
