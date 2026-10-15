// Location: app/src/main/java/com/vk/clockdin/ui/paint/PaintModels.kt
package com.vk.clockdin.ui.paint

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.vk.clockdin.domain.model.MoodCategory
import com.vk.clockdin.domain.model.MoodColor
import java.util.UUID

enum class PaintMode {
    FREEHAND,
    GRADIENT
}

/**
 * A single freehand brush stroke containing continuous points, color, and brush radius.
 */
data class PaintStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

/**
 * A radial gradient stop in gradient builder mode.
 * Position represents normalized distance from center (0.0 to 1.0).
 */
data class GradientBuilderStop(
    val id: String = UUID.randomUUID().toString(),
    val color: Color,
    val position: Float,
    val moodColor: MoodColor? = null
)

data class PaintUiState(
    val mode: PaintMode = PaintMode.FREEHAND,
    val availableColors: List<MoodColor> = emptyList(),
    val selectedColors: List<MoodColor> = emptyList(),
    val activeColor: MoodColor? = null,
    val strokes: List<PaintStroke> = emptyList(),
    val gradientStops: List<GradientBuilderStop> = emptyList(),
    val brushSize: Float = 32f,
    val isShaking: Boolean = false,
    val validationMessage: String? = null,
    val isRasterizing: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)

sealed interface PaintEvent {
    data class ProceedToSave(
        val orbImagePath: String,
        val dominantColors: List<com.vk.clockdin.domain.model.GradientStop>
    ) : PaintEvent
    data object ShowDiscardConfirm : PaintEvent
    data object TriggerHaptic : PaintEvent
}
