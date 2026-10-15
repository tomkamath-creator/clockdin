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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.vk.clockdin.domain.model.MoodColor
import com.vk.clockdin.ui.galaxy.GalaxyShader
import com.vk.clockdin.ui.theme.GlassBorderHairline
import kotlin.math.sqrt

/**
 * Interactive circular painting canvas for mood orb creation.
 * Supports both Freehand brush drawing and Radial Gradient builder manipulation.
 */
@Composable
fun PaintCanvas(
    mode: PaintMode,
    strokes: List<PaintStroke>,
    gradientStops: List<GradientBuilderStop>,
    activeColor: MoodColor?,
    brushSize: Float,
    onAddStroke: (PaintStroke) -> Unit,
    onUpdateGradientStopPosition: (id: String, position: Float) -> Unit,
    onRemoveGradientStop: (id: String) -> Unit,
    onCanvasSizeMeasured: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current in-progress stroke for freehand drawing
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var activeDraggingStopId by remember { mutableStateOf<String?>(null) }

    val resolvedActiveColor = remember(activeColor) {
        activeColor?.let { parseColor(it.hex) } ?: Color(0xFFFFC83D)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow matching the dominant / active mood
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
                .background(Color(0xFF06070B))
                .border(1.dp, GlassBorderHairline, CircleShape)
                .onGloballyPositioned { coordinates ->
                    onCanvasSizeMeasured(coordinates.size.width.toFloat())
                }
                .pointerInput(mode, resolvedActiveColor, brushSize) {
                    if (mode == PaintMode.FREEHAND) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints = currentPoints + change.position
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    onAddStroke(
                                        PaintStroke(
                                            points = currentPoints,
                                            color = resolvedActiveColor,
                                            strokeWidth = brushSize
                                        )
                                    )
                                }
                                currentPoints = emptyList()
                            },
                            onDragCancel = {
                                currentPoints = emptyList()
                            }
                        )
                    }
                }
                .pointerInput(mode, resolvedActiveColor, brushSize) {
                    if (mode == PaintMode.FREEHAND) {
                        detectTapGestures(
                            onTap = { offset ->
                                onAddStroke(
                                    PaintStroke(
                                        points = listOf(offset),
                                        color = resolvedActiveColor,
                                        strokeWidth = brushSize
                                    )
                                )
                            }
                        )
                    }
                }
                .pointerInput(mode, gradientStops) {
                    if (mode == PaintMode.GRADIENT) {
                        detectDragGestures(
                            onDragStart = { touchOffset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val radius = size.width / 2f
                                val touchThresholdPx = 48.dp.toPx()

                                // Find nearest stop handle along the ray
                                val nearestStop = gradientStops.minByOrNull { stop ->
                                    val handlePos = Offset(
                                        x = center.x + stop.position * radius,
                                        y = center.y
                                    )
                                    (handlePos - touchOffset).getDistance()
                                }

                                if (nearestStop != null) {
                                    val handlePos = Offset(
                                        x = center.x + nearestStop.position * radius,
                                        y = center.y
                                    )
                                    if ((handlePos - touchOffset).getDistance() <= touchThresholdPx) {
                                        activeDraggingStopId = nearestStop.id
                                    }
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val stopId = activeDraggingStopId
                                if (stopId != null) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val radius = size.width / 2f
                                    val normalizedPos = ((change.position.x - center.x) / radius).coerceIn(0f, 1f)
                                    onUpdateGradientStopPosition(stopId, normalizedPos)
                                }
                            },
                            onDragEnd = {
                                activeDraggingStopId = null
                            },
                            onDragCancel = {
                                activeDraggingStopId = null
                            }
                        )
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f

                if (mode == PaintMode.GRADIENT && gradientStops.isNotEmpty()) {
                    // 1. Draw radial gradient
                    val sortedStops = gradientStops.sortedBy { it.position }
                    val colorStops = sortedStops.map {
                        it.position.coerceIn(0f, 1f) to it.color
                    }.toTypedArray()

                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = colorStops,
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    // 2. Draw gradient manipulation ray guide (horizontal from center to right)
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = center,
                        end = Offset(center.x + radius, center.y),
                        strokeWidth = 2.dp.toPx()
                    )

                    // 3. Draw draggable handle beads on the ray
                    gradientStops.forEach { stop ->
                        val handleX = center.x + stop.position * radius
                        val handleY = center.y
                        val isDragging = activeDraggingStopId == stop.id
                        val beadRadius = if (isDragging) 15.dp.toPx() else 11.dp.toPx()

                        // Outer halo
                        drawCircle(
                            color = stop.color.copy(alpha = 0.45f),
                            radius = beadRadius + 6.dp.toPx(),
                            center = Offset(handleX, handleY)
                        )
                        // Color bead body
                        drawCircle(
                            color = stop.color,
                            radius = beadRadius,
                            center = Offset(handleX, handleY)
                        )
                        // Crisp white border
                        drawCircle(
                            color = Color.White,
                            radius = beadRadius,
                            center = Offset(handleX, handleY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                } else {
                    // Freehand Brush Mode
                    // Render completed strokes
                    strokes.forEach { stroke ->
                        drawPaintStroke(stroke)
                    }

                    // Render active in-progress stroke
                    if (currentPoints.isNotEmpty()) {
                        drawPaintStroke(
                            PaintStroke(
                                points = currentPoints,
                                color = resolvedActiveColor,
                                strokeWidth = brushSize
                            )
                        )
                    }
                }

                // Inner subtle glass specular rim
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
 * Draws a smooth PaintStroke on Canvas Scope.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPaintStroke(stroke: PaintStroke) {
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
                // Quadratic bezier midpoint smoothing for fluid brush feel
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

private fun parseColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorLong = clean.toLong(16)
        if (clean.length == 6) Color(colorLong or 0xFF000000) else Color(colorLong)
    } catch (_: Exception) {
        Color(0xFFFFC83D)
    }
}
