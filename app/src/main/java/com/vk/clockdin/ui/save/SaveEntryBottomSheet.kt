// Location: app/src/main/java/com/vk/clockdin/ui/save/SaveEntryBottomSheet.kt
package com.vk.clockdin.ui.save

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.usecase.SaveEntryUseCase
import com.vk.clockdin.ui.theme.CosmicAbyss
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.GlassSurfaceUltraLow
import com.vk.clockdin.ui.theme.PureBlack
import com.vk.clockdin.ui.theme.TextDisabled
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.TextTertiary
import com.vk.clockdin.ui.theme.glassCard
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveEntryBottomSheet(
    orbImagePath: String,
    dominantColors: List<GradientStop>,
    saveEntryUseCase: SaveEntryUseCase,
    onDismiss: () -> Unit,
    onSaveSuccess: (MoodEntry) -> Unit,
    modifier: Modifier = Modifier,
    existingEntry: MoodEntry? = null,
    updateEntryUseCase: com.vk.clockdin.domain.usecase.UpdateEntryUseCase? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var noteText by remember { mutableStateOf(existingEntry?.note ?: "") }
    var attachedPhotoPaths by remember {
        mutableStateOf<List<String>>(existingEntry?.photoPaths?.filter { it.isNotBlank() } ?: emptyList())
    }
    var isSaving by remember { mutableStateOf(false) }
    var isProcessingPhoto by remember { mutableStateOf(false) }

    // Camera capture state
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // ActivityResult Launcher for Camera (appends photo)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            scope.launch {
                isProcessingPhoto = true
                val compressedPath = PhotoCompressor.processAndSavePhoto(context, pendingCameraUri!!)
                if (compressedPath != null && attachedPhotoPaths.size < 6) {
                    attachedPhotoPaths = attachedPhotoPaths + compressedPath
                }
                isProcessingPhoto = false
            }
        }
    }

    // ActivityResult Launcher for Gallery (PickMultipleVisualMedia up to 6 items)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6)
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            scope.launch {
                isProcessingPhoto = true
                val newPaths = mutableListOf<String>()
                for (uri in uris) {
                    if (attachedPhotoPaths.size + newPaths.size >= 6) break
                    val compressedPath = PhotoCompressor.processAndSavePhoto(context, uri)
                    if (compressedPath != null) {
                        newPaths.add(compressedPath)
                    }
                }
                attachedPhotoPaths = attachedPhotoPaths + newPaths
                isProcessingPhoto = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CosmicAbyss.copy(alpha = 0.96f),
        scrimColor = PureBlack.copy(alpha = 0.70f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.25f))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Orb Thumbnail Preview & Mood Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Orb image
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(1.dp, GlassBorderHairline, CircleShape)
                        .background(Color(0xFF07080E)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(orbImagePath),
                        contentDescription = "Painted Mood Orb",
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (existingEntry != null) "Update Galaxy Orb" else "Journal Today's Orb",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Dominant mood badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dominantColors.forEach { stop ->
                            val c = parseColor(stop.hex)
                            Row(
                                modifier = Modifier
                                    .glassCard(shape = RoundedCornerShape(10.dp), fillColor = GlassSurfaceUltraLow)
                                    .border(1.dp, c.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                )
                                Text(
                                    text = stop.label ?: stop.hex,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 2. Note Input (max 500 characters)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .glassCard(shape = RoundedCornerShape(18.dp), fillColor = GlassSurfaceLow)
                        .padding(14.dp)
                ) {
                    if (noteText.isEmpty()) {
                        Text(
                            text = "What was on your mind today? Write a thought or memory...",
                            color = TextTertiary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                    BasicTextField(
                        value = noteText,
                        onValueChange = { if (it.length <= 500) noteText = it },
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Counter
                Text(
                    text = "${noteText.length}/500",
                    color = if (noteText.length >= 480) Color(0xFFFF5376) else TextDisabled,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // 4. Photo Attachment (Multiple Photos Support)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (attachedPhotoPaths.isEmpty()) "Photo Memories (optional)" else "Photo Memories (${attachedPhotoPaths.size}/6)",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    if (attachedPhotoPaths.isNotEmpty()) {
                        Text(
                            text = "Add up to 6",
                            fontSize = 11.sp,
                            color = TextDisabled
                        )
                    }
                }

                if (attachedPhotoPaths.isNotEmpty()) {
                    // Horizontal scroll of attached photos with individual remove buttons + Add more button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        attachedPhotoPaths.forEachIndexed { index, path ->
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, GlassBorderHairline, RoundedCornerShape(14.dp))
                            ) {
                                AsyncImage(
                                    model = File(path),
                                    contentDescription = "Attached Photo $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Individual remove button
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xD9E53935))
                                        .clickable {
                                            attachedPhotoPaths = attachedPhotoPaths.toMutableList().also { it.removeAt(index) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        // Add More button if under limit
                        if (attachedPhotoPaths.size < 6) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .glassCard(shape = RoundedCornerShape(14.dp), fillColor = GlassSurfaceUltraLow)
                                    .clickable(enabled = !isProcessingPhoto) {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Add More",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("+ Add", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Camera Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .glassCard(shape = RoundedCornerShape(14.dp), fillColor = GlassSurfaceUltraLow)
                                .clickable(enabled = !isProcessingPhoto) {
                                    val (uri, _) = PhotoCompressor.createCameraDestinationUri(context)
                                    pendingCameraUri = uri
                                    cameraLauncher.launch(uri)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Camera",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text("Camera", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Gallery Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .glassCard(shape = RoundedCornerShape(14.dp), fillColor = GlassSurfaceUltraLow)
                                .clickable(enabled = !isProcessingPhoto) {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Gallery",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text("Gallery", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                if (isProcessingPhoto) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = TextPrimary)
                        Text("Compressing photo...", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 5. "Save to Galaxy" Glowing Action Button
            val primaryGlowColor = remember(dominantColors) {
                dominantColors.firstOrNull()?.let { parseColor(it.hex) } ?: Color(0xFFFFC83D)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryGlowColor.copy(alpha = 0.50f),
                                primaryGlowColor.copy(alpha = 0.20f)
                            )
                        )
                    )
                    .border(1.5.dp, primaryGlowColor.copy(alpha = 0.85f), RoundedCornerShape(25.dp))
                    .clickable(enabled = !isSaving) {
                        isSaving = true
                        scope.launch {
                            if (existingEntry != null && updateEntryUseCase != null) {
                                val updated = existingEntry.copy(
                                    gradientStops = dominantColors,
                                    orbImagePath = orbImagePath,
                                    note = noteText.ifBlank { null },
                                    photoPath = if (attachedPhotoPaths.isEmpty()) null else attachedPhotoPaths.joinToString("||")
                                )
                                updateEntryUseCase(updated)
                                isSaving = false
                                onSaveSuccess(updated)
                            } else {
                                val newEntry = MoodEntry(
                                    createdAt = System.currentTimeMillis(),
                                    localDate = LocalDate.now().toString(),
                                    gradientStops = dominantColors,
                                    orbImagePath = orbImagePath,
                                    note = noteText.ifBlank { null },
                                    photoPath = if (attachedPhotoPaths.isEmpty()) null else attachedPhotoPaths.joinToString("||"),
                                    songTitle = null,
                                    songArtist = null
                                )
                                val insertedId = saveEntryUseCase(newEntry)
                                isSaving = false
                                onSaveSuccess(newEntry.copy(id = insertedId))
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (existingEntry != null) "Update Galaxy Orb ✦" else "Save to Galaxy ✦",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
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
