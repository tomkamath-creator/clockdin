// Location: app/src/main/java/com/vk/clockdin/ui/detail/EntryDetailScreen.kt
package com.vk.clockdin.ui.detail

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.GlassSurfaceUltraLow
import com.vk.clockdin.ui.theme.MoodAngry
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
fun EntryDetailScreen(
    onBack: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EntryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .spaceBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (val state = uiState) {
            is EntryDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TextPrimary, strokeWidth = 2.dp)
                }
            }
            is EntryDetailUiState.NotFound -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Mood Orb Not Found", color = TextSecondary, fontSize = 16.sp)
                        Box(
                            modifier = Modifier
                                .glassCard(shape = RoundedCornerShape(18.dp), fillColor = GlassSurfaceLow)
                                .clickable { onBack() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Go Back", color = TextPrimary)
                        }
                    }
                }
            }
            is EntryDetailUiState.Success -> {
                val entry = state.entry
                val formattedDate = remember(entry.localDate) {
                    try {
                        val parsed = LocalDate.parse(entry.localDate)
                        parsed.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()))
                    } catch (_: Exception) {
                        entry.localDate
                    }
                }

                val primaryColor = remember(entry.gradientStops) {
                    entry.gradientStops.firstOrNull()?.let { parseColor(it.hex) } ?: Color(0xFFFFC83D)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .glassCard(shape = CircleShape, fillColor = GlassSurfaceLow)
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

                        // Date Header
                        Text(
                            text = formattedDate,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Delete Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .glassCard(shape = CircleShape, fillColor = GlassSurfaceLow)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { showDeleteConfirmDialog = true }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Orb",
                                tint = Color(0xFFFF5376),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Hero Orb with Ambient Glow
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val glowRadius = size.width / 2f * 1.35f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.50f),
                                        primaryColor.copy(alpha = 0.15f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = glowRadius
                                ),
                                radius = glowRadius,
                                center = center
                            )
                        }

                        // Orb image
                        Box(
                            modifier = Modifier
                                .size(190.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, GlassBorderHairline, CircleShape)
                                .background(Color(0xFF07080E)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = File(entry.orbImagePath),
                                contentDescription = "Mood Orb",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // 3. Dominant Mood Tags
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        entry.gradientStops.forEach { stop ->
                            val c = parseColor(stop.hex)
                            Row(
                                modifier = Modifier
                                    .glassCard(shape = RoundedCornerShape(14.dp), fillColor = GlassSurfaceLow)
                                    .border(1.dp, c.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                )
                                Text(
                                    text = stop.label ?: stop.hex,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Journal Note (if present)
                    if (!entry.note.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .glassCard(shape = RoundedCornerShape(20.dp), fillColor = GlassSurfaceLow)
                                .padding(18.dp)
                        ) {
                            Text(
                                text = entry.note,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    // 6. Attached Photo Memory (if present)
                    if (!entry.photoPath.isNullOrBlank()) {
                        val photoFile = File(entry.photoPath)
                        if (photoFile.exists()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .border(1.dp, GlassBorderHairline, RoundedCornerShape(22.dp))
                                    .clickable { onOpenPhoto(entry.photoPath) }
                            ) {
                                AsyncImage(
                                    model = photoFile,
                                    contentDescription = "Photo Memory",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    contentScale = ContentScale.Crop
                                )

                                // Tap to expand hint
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                        .glassCard(shape = RoundedCornerShape(12.dp), fillColor = Color.Black.copy(alpha = 0.60f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text("Tap to enlarge", color = TextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Delete Confirmation Dialog
                if (showDeleteConfirmDialog) {
                    DeleteConfirmationDialog(
                        onDismiss = { showDeleteConfirmDialog = false },
                        onConfirmDelete = {
                            showDeleteConfirmDialog = false
                            viewModel.deleteEntry(onDeleted = onBack)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .glassCard(shape = RoundedCornerShape(26.dp), fillColor = GlassSurfaceMedium)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Delete this mood orb?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = "This orb, reflection note, and attached photo will be permanently removed from your galaxy.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .glassCard(shape = RoundedCornerShape(24.dp), fillColor = GlassSurfaceLow)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    // Bold, vivid high-contrast Delete button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE53935), Color(0xFFD32F2F))
                                )
                            )
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(24.dp))
                            .clickable(onClick = onConfirmDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Delete",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
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
        val l = clean.toLong(16)
        if (clean.length == 6) Color(l or 0xFF000000) else Color(l)
    } catch (_: Exception) {
        Color(0xFFFFC83D)
    }
}
