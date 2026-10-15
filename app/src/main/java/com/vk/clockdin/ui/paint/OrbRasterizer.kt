// Location: app/src/main/java/com/vk/clockdin/ui/paint/OrbRasterizer.kt
package com.vk.clockdin.ui.paint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import com.vk.clockdin.domain.model.GradientStop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

/**
 * Utility for rasterizing the painted orb to a crisp 512x512 circular transparent PNG
 * and extracting dominant gradient stops.
 */
object OrbRasterizer {

    private const val RASTER_SIZE = 512

    /**
     * Rasterizes the current paint canvas to a 512x512 transparent circular PNG.
     *
     * @param context Application context for accessing filesDir/orbs.
     * @param mode Freehand brush or Gradient builder.
     * @param strokes Freehand strokes with screen-space coordinates.
     * @param canvasSize Width/height of the on-screen canvas view.
     * @param gradientStops Stops if in gradient mode.
     * @return Absolute file path to the generated PNG.
     */
    suspend fun rasterizeToFile(
        context: Context,
        mode: PaintMode,
        strokes: List<PaintStroke>,
        canvasSize: Float,
        gradientStops: List<GradientBuilderStop>
    ): String = withContext(Dispatchers.IO) {
        val bitmap = Bitmap.createBitmap(RASTER_SIZE, RASTER_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val center = RASTER_SIZE / 2f
        val radius = RASTER_SIZE / 2f

        // Clip strictly to a circular mask
        val clipPath = Path().apply {
            addCircle(center, center, radius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        if (mode == PaintMode.GRADIENT && gradientStops.isNotEmpty()) {
            renderGradient(canvas, center, radius, gradientStops)
        } else {
            renderFreehand(canvas, strokes, canvasSize, center)
        }

        // Save to app-private filesDir/orbs/
        val orbsDir = File(context.filesDir, "orbs").also { it.mkdirs() }
        val outputFile = File(orbsDir, "orb_${System.currentTimeMillis()}.png")

        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()

        outputFile.absolutePath
    }

    private fun renderGradient(
        canvas: Canvas,
        center: Float,
        radius: Float,
        stops: List<GradientBuilderStop>
    ) {
        val sortedStops = stops.sortedBy { it.position }
        val colors = IntArray(sortedStops.size) { i ->
            val c = sortedStops[i].color
            android.graphics.Color.argb(
                (c.alpha * 255).toInt(),
                (c.red * 255).toInt(),
                (c.green * 255).toInt(),
                (c.blue * 255).toInt()
            )
        }
        val positions = FloatArray(sortedStops.size) { i ->
            sortedStops[i].position.coerceIn(0f, 1f)
        }

        val shader = RadialGradient(
            center, center, radius,
            colors, positions,
            Shader.TileMode.CLAMP
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.shader = shader
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, radius, paint)
    }

    private fun renderFreehand(
        canvas: Canvas,
        strokes: List<PaintStroke>,
        canvasSize: Float,
        center: Float
    ) {
        val scale = if (canvasSize > 0f) RASTER_SIZE / canvasSize else 1f

        // Draw soft base fill
        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(255, 18, 19, 26)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, center, basePaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (stroke in strokes) {
            val pts = stroke.points
            if (pts.isEmpty()) continue

            val c = stroke.color
            strokePaint.color = android.graphics.Color.argb(
                (c.alpha * 255).toInt(),
                (c.red * 255).toInt(),
                (c.green * 255).toInt(),
                (c.blue * 255).toInt()
            )
            strokePaint.strokeWidth = stroke.strokeWidth * scale

            if (pts.size == 1) {
                val p = pts[0]
                canvas.drawCircle(p.x * scale, p.y * scale, (stroke.strokeWidth * scale) / 2f, strokePaint)
            } else {
                val path = Path()
                path.moveTo(pts[0].x * scale, pts[0].y * scale)
                for (i in 1 until pts.size) {
                    path.lineTo(pts[i].x * scale, pts[i].y * scale)
                }
                canvas.drawPath(path, strokePaint)
            }
        }
    }

    /**
     * Extracts 2-5 dominant gradient stops from the rendered orb or stops list.
     */
    fun extractDominantStops(
        mode: PaintMode,
        gradientStops: List<GradientBuilderStop>,
        strokes: List<PaintStroke>,
        fallbackColors: List<com.vk.clockdin.domain.model.MoodColor>
    ): List<GradientStop> {
        if (mode == PaintMode.GRADIENT && gradientStops.isNotEmpty()) {
            return gradientStops.sortedBy { it.position }.map { stop ->
                val hex = String.format("#%02X%02X%02X",
                    (stop.color.red * 255).toInt(),
                    (stop.color.green * 255).toInt(),
                    (stop.color.blue * 255).toInt()
                )
                GradientStop(
                    hex = hex,
                    position = stop.position,
                    label = stop.moodColor?.name
                )
            }
        }

        // In freehand mode, extract from distinct colors used in strokes
        val distinctColors = strokes.map { it.color }.distinct()
        if (distinctColors.isNotEmpty()) {
            val count = distinctColors.size.coerceIn(2, 5)
            return distinctColors.take(count).mapIndexed { index, c ->
                val hex = String.format("#%02X%02X%02X",
                    (c.red * 255).toInt(),
                    (c.green * 255).toInt(),
                    (c.blue * 255).toInt()
                )
                val pos = if (count > 1) index.toFloat() / (count - 1) else 0f
                GradientStop(hex = hex, position = pos)
            }
        }

        // Fallback to selected mood colors
        val safeColors = fallbackColors.ifEmpty {
            listOf(
                com.vk.clockdin.domain.model.MoodColor(0, "Joy", "#FFC83D", com.vk.clockdin.domain.model.MoodCategory.POSITIVE, 0),
                com.vk.clockdin.domain.model.MoodColor(1, "Calm", "#2FD4A7", com.vk.clockdin.domain.model.MoodCategory.POSITIVE, 1)
            )
        }
        return safeColors.take(5).mapIndexed { index, mc ->
            val pos = if (safeColors.size > 1) index.toFloat() / (safeColors.size - 1) else 0f
            GradientStop(hex = mc.hex, position = pos, label = mc.name)
        }
    }
}
