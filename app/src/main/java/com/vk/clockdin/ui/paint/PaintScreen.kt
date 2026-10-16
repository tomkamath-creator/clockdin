// Location: app/src/main/java/com/vk/clockdin/ui/paint/PaintScreen.kt
package com.vk.clockdin.ui.paint

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.ui.save.SaveEntryBottomSheet
import com.vk.clockdin.ui.theme.ClockdinMotion
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

@Composable
fun PaintScreen(
    onBack: () -> Unit,
    onProceedToSave: (orbImagePath: String, dominantColors: List<GradientStop>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaintViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    var showDiscardDialog by remember { mutableStateOf(false) }
    var measuredCanvasSize by remember { mutableFloatStateOf(512f) }

    // Save bottom sheet state
    var saveSheetPayload by remember { mutableStateOf<Pair<String, List<GradientStop>>?>(null) }

    // Intercept back gesture for unsaved changes confirmation
    BackHandler(enabled = true) {
        if (saveSheetPayload != null) {
            saveSheetPayload = null
        } else if (uiState.hasUnsavedChanges || uiState.strokes.isNotEmpty()) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    // Collect one-shot events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PaintEvent.ProceedToSave -> {
                    saveSheetPayload = Pair(event.orbImagePath, event.dominantColors)
                }
                is PaintEvent.ShowDiscardConfirm -> {
                    showDiscardDialog = true
                }
                is PaintEvent.TriggerHaptic -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .spaceBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar with Brush/Spray toggle, Undo, Clear, Next
            PaintTopBar(
                currentMode = uiState.mode,
                canUndo = uiState.strokes.isNotEmpty(),
                canClear = uiState.strokes.isNotEmpty() || uiState.hasUnsavedChanges,
                isRasterizing = uiState.isRasterizing,
                activeColor = uiState.activeColor,
                onModeChange = { viewModel.setMode(it) },
                onUndo = { viewModel.undoStroke() },
                onClear = { viewModel.clearCanvas() },
                onBack = {
                    if (uiState.hasUnsavedChanges || uiState.strokes.isNotEmpty()) {
                        showDiscardDialog = true
                    } else {
                        onBack()
                    }
                },
                onNext = { viewModel.onNextClicked(measuredCanvasSize) }
            )

            // 2. Central Circular Paint Canvas (Fluid Brush + Spray Paint)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                PaintCanvas(
                    mode = uiState.mode,
                    strokes = uiState.strokes,
                    activeColor = uiState.activeColor,
                    brushSize = uiState.brushSize,
                    onAddStroke = { viewModel.addStroke(it) },
                    onCanvasSizeMeasured = { size -> measuredCanvasSize = size },
                    onNoColorSelected = { viewModel.requestFirstColorSelection() },
                    modifier = Modifier.fillMaxSize(0.92f)
                )
            }

            // 3. Size selector for Brush width / Spray dispersion radius
            BrushSizeSelector(
                currentSize = uiState.brushSize,
                mode = uiState.mode,
                onSizeSelected = { viewModel.setBrushSize(it) }
            )

            // 4. Prominent, eye-catching Mood Palette Selector
            PaintPaletteSelector(
                availableColors = uiState.availableColors,
                selectedColors = uiState.selectedColors,
                activeColor = uiState.activeColor,
                isShaking = uiState.isShaking,
                validationMessage = uiState.validationMessage,
                onColorToggle = { viewModel.toggleMoodColor(it) },
                onActiveColorSelect = { viewModel.setActiveColor(it) },
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // Discard Confirmation Glass Dialog
        if (showDiscardDialog) {
            DiscardConfirmationDialog(
                onDismiss = { showDiscardDialog = false },
                onConfirmDiscard = {
                    showDiscardDialog = false
                    onBack()
                }
            )
        }

        // 5. Save Entry Glass Bottom Sheet
        saveSheetPayload?.let { (orbPath, colors) ->
            SaveEntryBottomSheet(
                orbImagePath = orbPath,
                dominantColors = colors,
                saveEntryUseCase = viewModel.saveEntryUseCase,
                onDismiss = { saveSheetPayload = null },
                onSaveSuccess = { savedEntry ->
                    saveSheetPayload = null
                    viewModel.clearCanvas()
                    // Immediately transition to Galaxy screen for seamless in-flight placement
                    onProceedToSave(savedEntry.orbImagePath, savedEntry.gradientStops)
                }
            )
        }
    }
}

/**
 * Brush size / Spray dispersion radius selector.
 */
@Composable
private fun BrushSizeSelector(
    currentSize: Float,
    mode: PaintMode,
    onSizeSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = remember(mode) {
        when (mode) {
            PaintMode.SPRAY -> listOf(
                50f to "Fine",
                100f to "Medium",
                160f to "Wide"
            )
            PaintMode.WATERCOLOR -> listOf(
                35f to "Fine",
                70f to "Medium",
                120f to "Broad"
            )
            PaintMode.BRUSH -> listOf(
                18f to "Fine",
                36f to "Medium",
                64f to "Broad"
            )
        }
    }

    Row(
        modifier = modifier
            .glassCard(shape = RoundedCornerShape(20.dp), fillColor = GlassSurfaceUltraLow)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when (mode) {
                PaintMode.SPRAY -> "Spray Radius:"
                PaintMode.WATERCOLOR -> "Wash Size:"
                PaintMode.BRUSH -> "Brush Size:"
            },
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )

        presets.forEach { (size, label) ->
            val isSelected = (currentSize == size)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color.White.copy(alpha = 0.14f) else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSizeSelected(size) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((size / 6f).coerceIn(6f, 15f).dp)
                        .clip(CircleShape)
                        .background(if (isSelected) TextPrimary else TextTertiary)
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) TextPrimary else TextTertiary
                )
            }
        }
    }
}

/**
 * Glassmorphic predictive back discard confirmation dialog.
 */
@Composable
private fun DiscardConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmDiscard: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .glassCard(shape = RoundedCornerShape(28.dp), fillColor = GlassSurfaceMedium)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Discard mood painting?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = "Your painted mood orb will not be saved to your galaxy. Are you sure you want to discard it?",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Keep painting button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .glassCard(shape = RoundedCornerShape(22.dp), fillColor = GlassSurfaceLow)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Keep Painting",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    // Bold, vivid Discard confirmation button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(Color(0xFFE53935), Color(0xFFD32F2F))
                                )
                            )
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(22.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onConfirmDiscard
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Discard",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
