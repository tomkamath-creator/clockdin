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
    fun extractDominantStops_gradientMode_preservesSortedPositionsAndLabels() {
        val stops = listOf(
            GradientBuilderStop(
                color = Color(1f, 0f, 0f),
                position = 1f,
                moodColor = MoodColor(1, "Passionate", "#FF0000", MoodCategory.POSITIVE, 1)
            ),
            GradientBuilderStop(
                color = Color(0f, 0f, 1f),
                position = 0f,
                moodColor = MoodColor(2, "Calm", "#0000FF", MoodCategory.POSITIVE, 0)
            )
        )

        val result = OrbRasterizer.extractDominantStops(
            mode = PaintMode.GRADIENT,
            gradientStops = stops,
            strokes = emptyList(),
            fallbackColors = emptyList()
        )

        assertEquals(2, result.size)
        assertEquals(0f, result[0].position, 0.001f)
        assertEquals("#0000FF", result[0].hex)
        assertEquals("Calm", result[0].label)

        assertEquals(1f, result[1].position, 0.001f)
        assertEquals("#FF0000", result[1].hex)
        assertEquals("Passionate", result[1].label)
    }

    @Test
    fun extractDominantStops_freehandMode_extractsDistinctColors() {
        val strokes = listOf(
            PaintStroke(
                points = listOf(Offset(10f, 10f)),
                color = Color(1f, 0.5f, 0f),
                strokeWidth = 24f
            ),
            PaintStroke(
                points = listOf(Offset(20f, 20f)),
                color = Color(0f, 0.8f, 0.4f),
                strokeWidth = 24f
            )
        )

        val result = OrbRasterizer.extractDominantStops(
            mode = PaintMode.FREEHAND,
            gradientStops = emptyList(),
            strokes = strokes,
            fallbackColors = emptyList()
        )

        assertEquals(2, result.size)
        assertEquals(0f, result[0].position, 0.001f)
        assertEquals(1f, result[1].position, 0.001f)
        assertTrue(result[0].hex.startsWith("#"))
        assertTrue(result[1].hex.startsWith("#"))
    }

    @Test
    fun extractDominantStops_fallbackWhenEmpty_returnsAtLeastTwoStops() {
        val fallback = listOf(
            MoodColor(10, "Focus", "#3D82FF", MoodCategory.NEUTRAL, 0),
            MoodColor(11, "Flow", "#00E5FF", MoodCategory.POSITIVE, 1),
            MoodColor(12, "Grateful", "#FFB703", MoodCategory.POSITIVE, 2)
        )

        val result = OrbRasterizer.extractDominantStops(
            mode = PaintMode.FREEHAND,
            gradientStops = emptyList(),
            strokes = emptyList(),
            fallbackColors = fallback
        )

        assertEquals(3, result.size)
        assertEquals("#3D82FF", result[0].hex)
        assertEquals("Focus", result[0].label)
        assertEquals(0f, result[0].position, 0.001f)
        assertEquals(1f, result[2].position, 0.001f)
    }
}
