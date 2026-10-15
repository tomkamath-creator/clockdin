// Location: app/src/main/java/com/vk/clockdin/ui/paint/PaintCanvas.kt
package com.vk.clockdin.ui.paint

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.vk.clockdin.domain.model.MoodColor
import com.vk.clockdin.ui.galaxy.GalaxyShader
import com.vk.clockdin.ui.theme.GlassBorderHairline
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Interactive circular painting canvas for mood orb creation.
 * Supports both Fluid Freehand Brush and Cosmic Spray Paint inside the orb.
 */
@Composable
fun PaintCanvas(
    mode: PaintMode,
    strokes: List<PaintStroke>,
    activeColor: MoodColor?,
    brushSize: Float,
    onAddStroke: (PaintStroke) -> Unit,
    onCanvasSizeMeasured: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current in-progress stroke data
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var currentSprayDots by remember { mutableStateOf<List<SprayDot>>(emptyList()) }
    var activeTouchOffset by remember { mutableStateOf<Offset?>(null) }

    val resolvedActiveColor = remember(activeColor) {
        activeColor?.let { parseColor(it.hex) } ?: Color(0xFFFFC83D)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow matching the active mood
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val glowRadius = size.width / 2f * 1.25f

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    val shaderBrush = GalaxyShader.createGlowBrush(
                        centerX = center.x,
                        centerY = center.y,
                        radius = glowRadius,
                        color = resolvedActiveColor,
                        intensity = 0.55f
                    )
                    drawCircle(brush = shaderBrush, radius = glowRadius, center = center)
                } catch (_: Exception) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                resolvedActiveColor.copy(alpha = 0.40f),
                                resolvedActiveColor.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = glowRadius
                        ),
                        radius = glowRadius,
                        center = center
                    )
                }
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            resolvedActiveColor.copy(alpha = 0.40f),
                            resolvedActiveColor.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = center
                )
            }
        }

        // Circular Orb Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(0xFF07080E))
                .border(1.dp, GlassBorderHairline, CircleShape)
                .onGloballyPositioned { coordinates ->
                    onCanvasSizeMeasured(coordinates.size.width.toFloat())
                }
                .pointerInput(mode, resolvedActiveColor, brushSize) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            activeTouchOffset = offset
                            if (mode == PaintMode.BRUSH) {
                                currentPoints = listOf(offset)
                                currentSprayDots = emptyList()
                            } else {
                                currentPoints = emptyList()
                                currentSprayDots = generateSprayDots(offset, brushSize, count = 22)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            activeTouchOffset = change.position
                            if (mode == PaintMode.BRUSH) {
                                currentPoints = currentPoints + change.position
                            } else {
                                val newDots = generateSprayDots(change.position, brushSize, count = 16)
                                currentSprayDots = currentSprayDots + newDots
                            }
                        },
                        onDragEnd = {
                            if (mode == PaintMode.BRUSH && currentPoints.isNotEmpty()) {
                                onAddStroke(
                                    PaintStroke(
                                        mode = PaintMode.BRUSH,
                                        points = currentPoints,
                                        color = resolvedActiveColor,
                                        strokeWidth = brushSize
                                    )
                                )
                            } else if (mode == PaintMode.SPRAY && currentSprayDots.isNotEmpty()) {
                                onAddStroke(
                                    PaintStroke(
                                        mode = PaintMode.SPRAY,
                                        sprayDots = currentSprayDots,
                                        color = resolvedActiveColor,
                                        strokeWidth = brushSize
                                    )
                                )
                            }
                            currentPoints = emptyList()
                            currentSprayDots = emptyList()
                            activeTouchOffset = null
                        },
                        onDragCancel = {
                            currentPoints = emptyList()
                            currentSprayDots = emptyList()
                            activeTouchOffset = null
                        }
                    )
                }
                .pointerInput(mode, resolvedActiveColor, brushSize) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (mode == PaintMode.BRUSH) {
                                onAddStroke(
                                    PaintStroke(
                                        mode = PaintMode.BRUSH,
                                        points = listOf(offset),
                                        color = resolvedActiveColor,
                                        strokeWidth = brushSize
                                    )
                                )
                            } else {
                                // Spray burst on tap
                                val burstDots = generateSprayDots(offset, brushSize, count = 35)
                                onAddStroke(
                                    PaintStroke(
                                        mode = PaintMode.SPRAY,
                                        sprayDots = burstDots,
                                        color = resolvedActiveColor,
                                        strokeWidth = brushSize
                                    )
                                )
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f

                // 1. Render all committed strokes
                for (stroke in strokes) {
                    if (stroke.mode == PaintMode.SPRAY) {
                        drawSprayStroke(stroke)
                    } else {
                        drawBrushStroke(stroke)
                    }
                }

                // 2. Render active in-progress stroke
                if (mode == PaintMode.BRUSH && currentPoints.isNotEmpty()) {
                    drawBrushStroke(
                        PaintStroke(
                            mode = PaintMode.BRUSH,
                            points = currentPoints,
                            color = resolvedActiveColor,
                            strokeWidth = brushSize
                        )
                    )
                } else if (mode == PaintMode.SPRAY && currentSprayDots.isNotEmpty()) {
                    drawSprayDotsDirect(currentSprayDots, resolvedActiveColor)
                }

                // 3. Spray nozzle cursor ring when user is dragging in Spray mode
                activeTouchOffset?.let { touchPos ->
                    if (mode == PaintMode.SPRAY) {
                        drawCircle(
                            color = resolvedActiveColor.copy(alpha = 0.35f),
                            radius = brushSize / 2f,
                            center = touchPos,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }

                // 4. Inner subtle glass specular rim
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = radius - 1.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

/**
 * Generates randomized aerosol / spray paint droplets around a central point.
 */
private fun generateSprayDots(center: Offset, sprayDiameter: Float, count: Int): List<SprayDot> {
    val maxR = sprayDiameter / 2f
    val dots = ArrayList<SprayDot>(count)
    for (i in 0 until count) {
        // Uniform circular distribution with radial falloff
        val u = Random.nextFloat()
        val r = maxR * sqrt(u)
        val theta = Random.nextFloat() * (2f * Math.PI.toFloat())

        val x = center.x + r * cos(theta)
        val y = center.y + r * sin(theta)

        // Drop size ranges between 1.5dp and 3.5dp with central density
        val dotRadius = (1.2f + Random.nextFloat() * 2.2f)
        val falloff = (1f - (r / maxR).coerceIn(0f, 1f))
        val alpha = (0.25f + Random.nextFloat() * 0.70f) * (0.4f + falloff * 0.6f)

        dots.add(SprayDot(x = x, y = y, radius = dotRadius, alpha = alpha.coerceIn(0.1f, 1f)))
    }
    return dots
}

/**
 * Draws a smooth Brush PaintStroke with quadratic Bezier smoothing.
 */
private fun DrawScope.drawBrushStroke(stroke: PaintStroke) {
    if (stroke.points.isEmpty()) return

    if (stroke.points.size == 1) {
        drawCircle(
            color = stroke.color,
            radius = stroke.strokeWidth / 2f,
            center = stroke.points.first()
        )
    } else {
        val path = Path().apply {
            moveTo(stroke.points[0].x, stroke.points[0].y)
            for (i in 1 until stroke.points.size) {
                val p0 = stroke.points[i - 1]
                val p1 = stroke.points[i]
                val midX = (p0.x + p1.x) / 2f
                val midY = (p0.y + p1.y) / 2f
                quadraticTo(p0.x, p0.y, midX, midY)
            }
            lineTo(stroke.points.last().x, stroke.points.last().y)
        }

        drawPath(
            path = path,
            color = stroke.color,
            style = Stroke(
                width = stroke.strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Draws a Spray paint stroke consisting of multiple aerosol droplets.
 */
private fun DrawScope.drawSprayStroke(stroke: PaintStroke) {
    for (dot in stroke.sprayDots) {
        drawCircle(
            color = stroke.color.copy(alpha = (stroke.color.alpha * dot.alpha).coerceIn(0f, 1f)),
            radius = dot.radius,
            center = Offset(dot.x, dot.y)
        )
    }
}

private fun DrawScope.drawSprayDotsDirect(dots: List<SprayDot>, baseColor: Color) {
    for (dot in dots) {
        drawCircle(
            color = baseColor.copy(alpha = (baseColor.alpha * dot.alpha).coerceIn(0f, 1f)),
            radius = dot.radius,
            center = Offset(dot.x, dot.y)
        )
    }
}

private fun parseColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorLong = clean.toLong(16)
        if (clean.length == 6) Color(colorLong or 0xFF000000) else Color(colorLong)
    } catch (_: Exception) {
        Color(0xFFFFC83D)
    }
}
