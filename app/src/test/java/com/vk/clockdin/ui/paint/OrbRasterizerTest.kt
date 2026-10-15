// Location: app/src/test/java/com/vk/clockdin/ui/paint/OrbRasterizerTest.kt
package com.vk.clockdin.ui.paint

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.vk.clockdin.domain.model.MoodCategory
import com.vk.clockdin.domain.model.MoodColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrbRasterizerTest {

    @Test
    fun extractDominantStops_brushMode_extractsDistinctColors() {
        val strokes = listOf(
            PaintStroke(
                mode = PaintMode.BRUSH,
                points = listOf(Offset(10f, 10f)),
                color = Color(1f, 0f, 0f),
                strokeWidth = 24f
            ),
            PaintStroke(
                mode = PaintMode.BRUSH,
                points = listOf(Offset(20f, 20f)),
                color = Color(0f, 1f, 0f),
                strokeWidth = 24f
            )
        )

        val result = OrbRasterizer.extractDominantStops(
            strokes = strokes,
            fallbackColors = emptyList()
        )

        assertEquals(2, result.size)
        assertEquals(0f, result[0].position, 0.001f)
        assertEquals(1f, result[1].position, 0.001f)
        assertEquals("#FF0000", result[0].hex)
        assertEquals("#00FF00", result[1].hex)
    }

    @Test
    fun extractDominantStops_sprayMode_extractsSprayStrokeColors() {
        val sprayDots = listOf(
            SprayDot(x = 100f, y = 100f, radius = 2f, alpha = 0.8f),
            SprayDot(x = 105f, y = 102f, radius = 1.5f, alpha = 0.6f)
        )
        val strokes = listOf(
            PaintStroke(
                mode = PaintMode.SPRAY,
                sprayDots = sprayDots,
                color = Color(0f, 0f, 1f),
                strokeWidth = 36f
            ),
            PaintStroke(
                mode = PaintMode.SPRAY,
                sprayDots = sprayDots,
                color = Color(1f, 1f, 0f),
                strokeWidth = 36f
            )
        )

        val result = OrbRasterizer.extractDominantStops(
            strokes = strokes,
            fallbackColors = emptyList()
        )

        assertEquals(2, result.size)
        assertEquals("#0000FF", result[0].hex)
        assertEquals("#FFFF00", result[1].hex)
        assertEquals(0f, result[0].position, 0.001f)
        assertEquals(1f, result[1].position, 0.001f)
    }

    @Test
    fun extractDominantStops_fallbackWhenEmpty_returnsAtLeastTwoStops() {
        val fallback = listOf(
            MoodColor(10, "Focus", "#3D82FF", MoodCategory.NEUTRAL, 0),
            MoodColor(11, "Flow", "#00E5FF", MoodCategory.POSITIVE, 1),
            MoodColor(12, "Grateful", "#FFB703", MoodCategory.POSITIVE, 2)
        )

        val result = OrbRasterizer.extractDominantStops(
            strokes = emptyList(),
            fallbackColors = fallback
        )

        assertEquals(2, result.size)
        assertEquals("#3D82FF", result[0].hex)
        assertEquals("Focus", result[0].label)
        assertEquals(0f, result[0].position, 0.001f)
        assertEquals(1f, result[1].position, 0.001f)
    }
}
