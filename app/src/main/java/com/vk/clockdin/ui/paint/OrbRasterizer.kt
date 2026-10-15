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
import com.vk.clockdin.domain.model.MoodColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Utility for rasterizing the painted orb (freehand brush, spray paint, and watercolor)
 * to a crisp 512x512 circular transparent PNG and extracting dominant gradient stops.
 */
object OrbRasterizer {

    private const val RASTER_SIZE = 512

    /**
     * Rasterizes the current paint strokes to a 512x512 transparent circular PNG.
     *
     * @param context Application context for accessing filesDir/orbs.
     * @param strokes Freehand brush, spray, and watercolor strokes with screen-space coordinates.
     * @param canvasSize Width/height of the on-screen canvas view.
     * @return Absolute file path to the generated PNG.
     */
    suspend fun rasterizeToFile(
        context: Context,
        strokes: List<PaintStroke>,
        canvasSize: Float
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

        renderStrokes(canvas, strokes, canvasSize, center)

        // Save to app-private filesDir/orbs/
        val orbsDir = File(context.filesDir, "orbs").also { it.mkdirs() }
        val outputFile = File(orbsDir, "orb_${System.currentTimeMillis()}.png")

        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()

        outputFile.absolutePath
    }

    private fun renderStrokes(
        canvas: Canvas,
        strokes: List<PaintStroke>,
        canvasSize: Float,
        center: Float
    ) {
        val scale = if (canvasSize > 0f) RASTER_SIZE / canvasSize else 1f

        // Draw soft deep space base fill
        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(255, 10, 11, 16)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, center, basePaint)

        val brushPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val sprayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        val watercolorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (stroke in strokes) {
            val c = stroke.color
            val r = (c.red * 255).toInt()
            val g = (c.green * 255).toInt()
            val b = (c.blue * 255).toInt()

            when (stroke.mode) {
                PaintMode.SPRAY -> {
                    // Render spray droplets
                    for (dot in stroke.sprayDots) {
                        val alpha = (dot.alpha * c.alpha * 255).toInt().coerceIn(0, 255)
                        sprayPaint.color = android.graphics.Color.argb(alpha, r, g, b)
                        canvas.drawCircle(
                            dot.x * scale,
                            dot.y * scale,
                            (dot.radius * scale).coerceAtLeast(1f),
                            sprayPaint
                        )
                    }
                }
                PaintMode.WATERCOLOR -> {
                    // Render soft bleeding watercolor pigment washes
                    for (drop in stroke.watercolorDrops) {
                        val scaledR = drop.radius * scale
                        if (scaledR <= 0f) continue
                        val centerAlpha = (c.alpha * drop.alpha * 0.28f * 255).toInt().coerceIn(0, 255)
                        val midAlpha = (c.alpha * drop.alpha * 0.10f * 255).toInt().coerceIn(0, 255)

                        val shader = RadialGradient(
                            drop.x * scale, drop.y * scale, scaledR,
                            intArrayOf(
                                android.graphics.Color.argb(centerAlpha, r, g, b),
                                android.graphics.Color.argb(midAlpha, r, g, b),
                                android.graphics.Color.argb(0, r, g, b)
                            ),
                            floatArrayOf(0f, 0.65f, 1f),
                            Shader.TileMode.CLAMP
                        )
                        watercolorPaint.shader = shader
                        canvas.drawCircle(drop.x * scale, drop.y * scale, scaledR, watercolorPaint)
                    }
                    watercolorPaint.shader = null
                }
                PaintMode.BRUSH -> {
                    // Render solid fluid brush path
                    val pts = stroke.points
                    if (pts.isEmpty()) continue

                    brushPaint.color = android.graphics.Color.argb(
                        (c.alpha * 255).toInt(),
                        r, g, b
                    )
                    brushPaint.strokeWidth = stroke.strokeWidth * scale

                    if (pts.size == 1) {
                        val p = pts[0]
                        val fillCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = brushPaint.color
                            style = Paint.Style.FILL
                        }
                        canvas.drawCircle(
                            p.x * scale,
                            p.y * scale,
                            (stroke.strokeWidth * scale) / 2f,
                            fillCirclePaint
                        )
                    } else {
                        val path = Path()
                        path.moveTo(pts[0].x * scale, pts[0].y * scale)
                        for (i in 1 until pts.size) {
                            path.lineTo(pts[i].x * scale, pts[i].y * scale)
                        }
                        canvas.drawPath(path, brushPaint)
                    }
                }
            }
        }
    }

    /**
     * Extracts 2-5 dominant gradient stops from the painted strokes and fallback colors.
     */
    fun extractDominantStops(
        strokes: List<PaintStroke>,
        fallbackColors: List<MoodColor>
    ): List<GradientStop> {
        val distinctColors = strokes.map { it.color }.distinct()
        val hexStops = mutableListOf<String>()

        // Collect colors from strokes first
        for (c in distinctColors) {
            val hex = String.format("#%02X%02X%02X",
                (c.red * 255).toInt(),
                (c.green * 255).toInt(),
                (c.blue * 255).toInt()
            )
            if (!hexStops.contains(hex)) {
                hexStops.add(hex)
            }
            if (hexStops.size >= 5) break
        }

        // If fewer than 2 colors in strokes, augment with selected mood colors
        if (hexStops.size < 2) {
            for (fallback in fallbackColors) {
                val hex = fallback.hex.uppercase()
                if (!hexStops.contains(hex)) {
                    hexStops.add(hex)
                }
                if (hexStops.size >= 2) break
            }
        }

        // Fallback default if still under 2
        if (hexStops.size < 2) {
            if (!hexStops.contains("#FFC83D")) hexStops.add("#FFC83D")
            if (!hexStops.contains("#2FD4A7")) hexStops.add("#2FD4A7")
        }

        val total = hexStops.size.coerceIn(2, 5)
        return hexStops.take(total).mapIndexed { index, hex ->
            val pos = if (total > 1) index.toFloat() / (total - 1) else 0f
            val matchingLabel = fallbackColors.firstOrNull { it.hex.equals(hex, ignoreCase = true) }?.name
            GradientStop(hex = hex, position = pos, label = matchingLabel)
        }
    }
}
