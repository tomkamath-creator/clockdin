// Location: app/src/main/java/com/vk/clockdin/ui/paint/PaintScreen.kt
package com.vk.clockdin.ui.paint

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import com.vk.clockdin.ui.theme.ClockdinMotion
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassSurfaceHigh
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.MoodExhaustion
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

    // Intercept back gesture for unsaved changes confirmation
    BackHandler(enabled = true) {
        if (uiState.hasUnsavedChanges || uiState.strokes.isNotEmpty()) {
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
                    onProceedToSave(event.orbImagePath, event.dominantColors)
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
            // 1. Top Bar
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

            // 2. Central Circular Paint Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                PaintCanvas(
                    mode = uiState.mode,
                    strokes = uiState.strokes,
                    gradientStops = uiState.gradientStops,
                    activeColor = uiState.activeColor,
                    brushSize = uiState.brushSize,
                    onAddStroke = { viewModel.addStroke(it) },
                    onUpdateGradientStopPosition = { id, pos ->
                        viewModel.updateGradientStopPosition(id, pos)
                    },
                    onRemoveGradientStop = { viewModel.removeGradientStop(it) },
                    onCanvasSizeMeasured = { size -> measuredCanvasSize = size },
                    modifier = Modifier.fillMaxSize(0.88f)
                )
            }

            // 3. Middle controls: Brush size picker (Freehand) or Gradient helper (Gradient)
            if (uiState.mode == PaintMode.FREEHAND) {
                BrushSizeSelector(
                    currentSize = uiState.brushSize,
                    onSizeSelected = { viewModel.setBrushSize(it) }
                )
            } else {
                GradientModeHelper(
                    stopCount = uiState.gradientStops.size,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // 4. Bottom Palette Selector
            PaintPaletteSelector(
                availableColors = uiState.availableColors,
                selectedColors = uiState.selectedColors,
                activeColor = uiState.activeColor,
                isShaking = uiState.isShaking,
                validationMessage = uiState.validationMessage,
                onColorToggle = { viewModel.toggleMoodColor(it) },
                onActiveColorSelect = { viewModel.setActiveColor(it) },
                modifier = Modifier.padding(bottom = 8.dp)
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
    }
}

/**
 * Brush size presets selector for Freehand mode.
 */
@Composable
private fun BrushSizeSelector(
    currentSize: Float,
    onSizeSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        16f to "Fine",
        32f to "Medium",
        56f to "Broad"
    )

    Row(
        modifier = modifier
            .glassCard(shape = RoundedCornerShape(20.dp), surfaceAlpha = 0.05f)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        presets.forEach { (size, label) ->
            val isSelected = (currentSize == size)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
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
                        .size((size / 3f).coerceIn(6f, 16f).dp)
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

@Composable
private fun GradientModeHelper(
    stopCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .glassCard(shape = RoundedCornerShape(16.dp), surfaceAlpha = 0.04f)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF00E5FF))
        )
        Text(
            text = "Drag glowing beads along the radius ray ($stopCount/5 stops)",
            fontSize = 12.sp,
            color = TextSecondary
        )
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
                .glassCard(shape = RoundedCornerShape(28.dp), surfaceAlpha = 0.16f)
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
                            .glassCard(shape = RoundedCornerShape(22.dp), surfaceAlpha = 0.08f)
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

                    // Discard button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MoodExhaustion.copy(alpha = 0.35f))
                            .border(1.dp, MoodExhaustion.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
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
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF8599)
                        )
                    }
                }
            }
        }
    }
}
