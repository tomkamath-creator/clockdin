// Location: app/src/main/java/com/vk/clockdin/ui/detail/EntryDetailScreen.kt
package com.vk.clockdin.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextAlign
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
import com.vk.clockdin.ui.theme.TextDisabled
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.TextTertiary
import com.vk.clockdin.ui.theme.glassCard
import com.vk.clockdin.ui.theme.spaceBackground
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Floating futuristic space card detail view arranged in three vertical sections:
 * 1. Top: Small badge painted orb glowing brightly with its authentic mood colors.
 * 2. Middle: Clean photo carousel showing all attached pictures (tap to open full-screen).
 * 3. Bottom: Frosted glass journal section with date, mood tags, and written reflection.
 */
@Composable
fun EntryDetailScreen(
    onBack: () -> Unit,
    onOpenPhoto: (List<String>, Int) -> Unit,
    onRepaintOrb: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: EntryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }

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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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

                val photos = remember(entry.photoPath) { entry.photoPaths.filter { it.isNotBlank() } }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Navigation Top Bar
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

                        Text(
                            text = "STAR ARCHIVE",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Edit Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .glassCard(shape = CircleShape, fillColor = GlassSurfaceLow)
                                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { showEditSheet = true }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Entry",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x26FF5252))
                                    .border(1.dp, Color(0x66FF5252), CircleShape)
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
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Floating Futuristic Space Card
                    FuturisticSpaceCard(
                        entry = entry,
                        formattedDate = formattedDate,
                        primaryColor = primaryColor,
                        photos = photos,
                        onOpenPhoto = onOpenPhoto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent Edit Reflection & Photos button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(50.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33262A36))
                            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(20.dp))
                            .clickable { showEditSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Reflection & Photos",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Edit Reflection & Photos ✦",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Prominent Delete Mood Orb button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(50.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x26FF5252))
                            .border(1.dp, Color(0x66FF5252), RoundedCornerShape(20.dp))
                            .clickable { showDeleteConfirmDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Mood Orb",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Delete Mood Orb",
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Edit Entry Bottom Sheet
                if (showEditSheet) {
                    EditEntryBottomSheet(
                        entry = entry,
                        onDismiss = { showEditSheet = false },
                        onSave = { updatedNote, updatedPhotos ->
                            viewModel.updateEntry(updatedNote, updatedPhotos)
                        },
                        onRepaintOrb = {
                            onRepaintOrb(entry.id)
                        }
                    )
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

/**
 * Futuristic Space Card composed of three vertical sections:
 * Top: Glowing badge orb
 * Middle: Photo carousel
 * Bottom: Frosted glass journal section
 */
@Composable
private fun FuturisticSpaceCard(
    entry: MoodEntry,
    formattedDate: String,
    primaryColor: Color,
    photos: List<String>,
    onOpenPhoto: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassCard(shape = RoundedCornerShape(32.dp), fillColor = GlassSurfaceMedium)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        primaryColor.copy(alpha = 0.50f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ==========================================
            // TOP SECTION: Small Glowing Badge Painted Orb
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Multi-layered authentic mood glow aura
                Canvas(modifier = Modifier.size(130.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val glowRadius = size.width / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.65f),
                                primaryColor.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = glowRadius
                        ),
                        radius = glowRadius,
                        center = center
                    )
                }

                // Small badge painted orb
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                        .background(Color(0xFF07080E)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(entry.orbImagePath),
                        contentDescription = "Painted Mood Orb Badge",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // ==========================================
            // MIDDLE SECTION: Clean Photo Carousel
            // ==========================================
            if (photos.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { photos.size })

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) { page ->
                        val file = File(photos[page])
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(22.dp))
                                .border(1.dp, GlassBorderHairline, RoundedCornerShape(22.dp))
                                .clickable { onOpenPhoto(photos, page) }
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = "Attached memory photo $page",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Tap to enlarge hint badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .glassCard(shape = RoundedCornerShape(12.dp), fillColor = Color.Black.copy(alpha = 0.65f))
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
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text("Full View", color = TextPrimary, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Dot indicators if multiple photos
                    if (photos.size > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            photos.indices.forEach { index ->
                                val isSelected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) primaryColor else TextDisabled.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            } else {
                // Subtle cosmic separator when no photos are attached
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "✦  ·  ·  ·  ✦",
                        color = primaryColor.copy(alpha = 0.45f),
                        fontSize = 11.sp,
                        letterSpacing = 3.sp
                    )
                }
            }

            // ==========================================
            // BOTTOM SECTION: Frosted Glass Journal Section
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(22.dp), fillColor = GlassSurfaceUltraLow)
                    .border(1.dp, GlassBorderHairline, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date Header
                    Text(
                        text = formattedDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    // Authentic Mood Tags / Pills
                    if (entry.gradientStops.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            entry.gradientStops.forEach { stop ->
                                val c = parseColor(stop.hex)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(c.copy(alpha = 0.16f))
                                        .border(1.dp, c.copy(alpha = 0.50f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                    )
                                    Text(
                                        text = stop.label ?: stop.hex,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Journal Note Reflection
                    val noteText = entry.note
                    if (!noteText.isNullOrBlank()) {
                        Text(
                            text = noteText,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            lineHeight = 21.sp
                        )
                    } else {
                        Text(
                            text = "A quiet day recorded in the stars.",
                            fontSize = 13.sp,
                            color = TextTertiary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
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
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF1A1D26))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(26.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Delete this mood orb?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF)
                )

                Text(
                    text = "This orb, reflection note, and attached photos will be permanently removed from your galaxy.",
                    fontSize = 14.sp,
                    color = Color(0xFFE2E4EB),
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
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
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF262A36))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", color = Color(0xFFE2E4EB), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    // Bold, vivid high-contrast Crimson Delete button
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
