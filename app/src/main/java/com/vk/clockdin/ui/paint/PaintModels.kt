// Location: app/src/main/java/com/vk/clockdin/ui/paint/PaintModels.kt
package com.vk.clockdin.ui.paint

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodColor

enum class PaintMode {
    BRUSH,
    SPRAY,
    WATERCOLOR
}

/**
 * A single spray droplet particle within a spray paint stroke.
 */
data class SprayDot(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float
)

/**
 * A soft translucent pigment wash dab within a watercolor stroke.
 */
data class WatercolorDrop(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float
)

/**
 * A paint stroke representing a smooth solid brush stroke,
 * a cosmic aerosol spray particle cloud, or a soft fluid watercolor wash.
 */
data class PaintStroke(
    val mode: PaintMode = PaintMode.BRUSH,
    val points: List<Offset> = emptyList(),
    val sprayDots: List<SprayDot> = emptyList(),
    val watercolorDrops: List<WatercolorDrop> = emptyList(),
    val color: Color,
    val strokeWidth: Float
)

data class PaintUiState(
    val mode: PaintMode = PaintMode.BRUSH,
    val availableColors: List<MoodColor> = emptyList(),
    val selectedColors: List<MoodColor> = emptyList(),
    val activeColor: MoodColor? = null,
    val strokes: List<PaintStroke> = emptyList(),
    val brushSize: Float = 42f,
    val isShaking: Boolean = false,
    val validationMessage: String? = null,
    val isRasterizing: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)

sealed interface PaintEvent {
    data class ProceedToSave(
        val orbImagePath: String,
        val dominantColors: List<GradientStop>
    ) : PaintEvent
    data object ShowDiscardConfirm : PaintEvent
    data object TriggerHaptic : PaintEvent
}
