// Location: app/src/main/java/com/vk/clockdin/ui/detail/EditEntryBottomSheet.kt
package com.vk.clockdin.ui.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
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
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.ui.save.PhotoCompressor
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryBottomSheet(
    entry: MoodEntry,
    onDismiss: () -> Unit,
    onSave: (note: String?, photos: List<String>) -> Unit,
    onRepaintOrb: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var noteText by remember { mutableStateOf(entry.note ?: "") }
    var attachedPhotoPaths by remember {
        mutableStateOf<List<String>>(entry.photoPaths.filter { it.isNotBlank() })
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

    val primaryColor = remember(entry.gradientStops) {
        entry.gradientStops.firstOrNull()?.let { parseHex(it.hex) } ?: Color(0xFFFFC83D)
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Edit Memory Entry",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Update reflection notes, photos, or repainted orb art",
                        fontSize = 11.sp,
                        color = Color(0xFFB0B5C6)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x26FFFFFF))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 1. Orb Artwork & Repaint Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(20.dp), fillColor = GlassSurfaceLow)
                    .border(1.dp, GlassBorderHairline, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            .background(Color(0xFF07080E)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(entry.orbImagePath),
                            contentDescription = "Current Orb",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Star Orb Artwork",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Want to change or repaint your colors?",
                            fontSize = 11.sp,
                            color = Color(0xFFB0B5C6)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x33262A36))
                            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(16.dp))
                            .clickable {
                                onDismiss()
                                onRepaintOrb()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Repaint",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                }
            }

            // 2. Note Input (max 500 characters)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "REFLECTION NOTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB0B5C6),
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .glassCard(shape = RoundedCornerShape(18.dp), fillColor = GlassSurfaceLow)
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    if (noteText.isEmpty()) {
                        Text(
                            text = "Write your reflection or thoughts...",
                            color = TextTertiary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                    BasicTextField(
                        value = noteText,
                        onValueChange = { if (it.length <= 500) noteText = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(Color.White),
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

            // 3. Photo Attachment & Editing (Multiple Photos Support)
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
                        text = "PHOTO MEMORIES (${attachedPhotoPaths.size}/6)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB0B5C6),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Tap ✕ to remove photo",
                        fontSize = 11.sp,
                        color = TextDisabled
                    )
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
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, GlassBorderHairline, RoundedCornerShape(16.dp))
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
                                        .size(22.dp)
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
                                    .size(80.dp)
                                    .glassCard(shape = RoundedCornerShape(16.dp), fillColor = GlassSurfaceUltraLow)
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
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
                                .height(46.dp)
                                .glassCard(shape = RoundedCornerShape(14.dp), fillColor = GlassSurfaceUltraLow)
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
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
                                .height(46.dp)
                                .glassCard(shape = RoundedCornerShape(14.dp), fillColor = GlassSurfaceUltraLow)
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
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

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Save Changes Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.85f),
                                primaryColor.copy(alpha = 0.50f)
                            )
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(25.dp))
                    .clickable(enabled = !isSaving) {
                        isSaving = true
                        onSave(noteText.ifBlank { null }, attachedPhotoPaths)
                        isSaving = false
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save Changes ✦",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun parseHex(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorLong = clean.toLong(16)
        if (clean.length == 6) Color(colorLong or 0xFF000000) else Color(colorLong)
    } catch (_: Exception) {
        Color(0xFFFFC83D)
    }
}
