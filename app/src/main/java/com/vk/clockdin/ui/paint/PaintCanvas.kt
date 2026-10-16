// Location: app/src/main/java/com/vk/clockdin/ui/paint/PaintCanvas.kt
package com.vk.clockdin.ui.paint

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
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
    onNoColorSelected: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Current in-progress stroke data
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var currentSprayDots by remember { mutableStateOf<List<SprayDot>>(emptyList()) }
    var currentWatercolorDrops by remember { mutableStateOf<List<WatercolorDrop>>(emptyList()) }
    var lastDropOffset by remember { mutableStateOf<Offset?>(null) }
    var activeTouchOffset by remember { mutableStateOf<Offset?>(null) }

    val resolvedActiveColor = remember(activeColor) {
        activeColor?.let { parseColor(it.hex) }
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
            val glowColor = resolvedActiveColor ?: Color(0x283F51B5)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    val shaderBrush = GalaxyShader.createGlowBrush(
                        centerX = center.x,
                        centerY = center.y,
                        radius = glowRadius,
                        color = glowColor,
                        intensity = if (resolvedActiveColor != null) 0.55f else 0.25f
                    )
                    drawCircle(brush = shaderBrush, radius = glowRadius, center = center)
                } catch (_: Exception) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = if (resolvedActiveColor != null) 0.40f else 0.15f),
                                glowColor.copy(alpha = 0.05f),
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
                            glowColor.copy(alpha = if (resolvedActiveColor != null) 0.40f else 0.15f),
                            glowColor.copy(alpha = 0.05f),
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
                            if (resolvedActiveColor == null) {
                                onNoColorSelected()
                                return@detectDragGestures
                            }
                            activeTouchOffset = offset
                            lastDropOffset = offset
                            when (mode) {
                                PaintMode.BRUSH -> {
                                    currentPoints = listOf(offset)
                                    currentSprayDots = emptyList()
                                    currentWatercolorDrops = emptyList()
                                }
                                PaintMode.SPRAY -> {
                                    currentPoints = emptyList()
                                    currentSprayDots = generateSprayDots(offset, brushSize, count = 55)
                                    currentWatercolorDrops = emptyList()
                                }
                                PaintMode.WATERCOLOR -> {
                                    currentPoints = emptyList()
                                    currentSprayDots = emptyList()
                                    currentWatercolorDrops = generateWatercolorDrops(offset, brushSize, count = 2)
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            if (resolvedActiveColor == null) return@detectDragGestures
                            change.consume()
                            val pos = change.position
                            activeTouchOffset = pos

                            when (mode) {
                                PaintMode.BRUSH -> {
                                    currentPoints = currentPoints + pos
                                }
                                PaintMode.SPRAY -> {
                                    val newDots = generateSprayDots(pos, brushSize, count = 45)
                                    currentSprayDots = currentSprayDots + newDots
                                }
                                PaintMode.WATERCOLOR -> {
                                    val last = lastDropOffset
                                    val minDistance = (brushSize * 0.32f).coerceAtLeast(14f)
                                    if (last == null || (pos - last).getDistance() >= minDistance) {
                                        val newDrops = generateWatercolorDrops(pos, brushSize, count = 1)
                                        currentWatercolorDrops = currentWatercolorDrops + newDrops
                                        lastDropOffset = pos
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            val activePaintColor = resolvedActiveColor ?: return@detectDragGestures
                            when (mode) {
                                PaintMode.BRUSH -> {
                                    if (currentPoints.isNotEmpty()) {
                                        onAddStroke(
                                            PaintStroke(
                                                mode = PaintMode.BRUSH,
                                                points = currentPoints,
                                                color = activePaintColor,
                                                strokeWidth = brushSize
                                            )
                                        )
                                    }
                                }
                                PaintMode.SPRAY -> {
                                    if (currentSprayDots.isNotEmpty()) {
                                        onAddStroke(
                                            PaintStroke(
                                                mode = PaintMode.SPRAY,
                                                sprayDots = currentSprayDots,
                                                color = activePaintColor,
                                                strokeWidth = brushSize
                                            )
                                        )
                                    }
                                }
                                PaintMode.WATERCOLOR -> {
                                    if (currentWatercolorDrops.isNotEmpty()) {
                                        onAddStroke(
                                            PaintStroke(
                                                mode = PaintMode.WATERCOLOR,
                                                watercolorDrops = currentWatercolorDrops,
                                                color = activePaintColor,
                                                strokeWidth = brushSize
                                            )
                                        )
                                    }
                                }
                            }
                            currentPoints = emptyList()
                            currentSprayDots = emptyList()
                            currentWatercolorDrops = emptyList()
                            lastDropOffset = null
                            activeTouchOffset = null
                        },
                        onDragCancel = {
                            currentPoints = emptyList()
                            currentSprayDots = emptyList()
                            currentWatercolorDrops = emptyList()
                            lastDropOffset = null
                            activeTouchOffset = null
                        }
                    )
                }
                .pointerInput(mode, resolvedActiveColor, brushSize) {
                    detectTapGestures(
                        onTap = { offset ->
                            val activePaintColor = resolvedActiveColor
                            if (activePaintColor == null) {
                                onNoColorSelected()
                                return@detectTapGestures
                            }
                            when (mode) {
                                PaintMode.BRUSH -> {
                                    onAddStroke(
                                        PaintStroke(
                                            mode = PaintMode.BRUSH,
                                            points = listOf(offset),
                                            color = activePaintColor,
                                            strokeWidth = brushSize
                                        )
                                    )
                                }
                                PaintMode.SPRAY -> {
                                    val burstDots = generateSprayDots(offset, brushSize, count = 90)
                                    onAddStroke(
                                        PaintStroke(
                                            mode = PaintMode.SPRAY,
                                            sprayDots = burstDots,
                                            color = activePaintColor,
                                            strokeWidth = brushSize
                                        )
                                    )
                                }
                                PaintMode.WATERCOLOR -> {
                                    val tapDrops = generateWatercolorDrops(offset, brushSize, count = 3)
                                    onAddStroke(
                                        PaintStroke(
                                            mode = PaintMode.WATERCOLOR,
                                            watercolorDrops = tapDrops,
                                            color = activePaintColor,
                                            strokeWidth = brushSize
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
        ) {
            val activePaintColor = resolvedActiveColor ?: Color.Transparent

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f

                // 1. Render all committed strokes
                for (stroke in strokes) {
                    when (stroke.mode) {
                        PaintMode.SPRAY -> drawSprayStroke(stroke)
                        PaintMode.WATERCOLOR -> drawWatercolorStroke(stroke)
                        PaintMode.BRUSH -> drawBrushStroke(stroke)
                    }
                }

                // 2. Render active in-progress stroke
                when (mode) {
                    PaintMode.BRUSH -> {
                        if (currentPoints.isNotEmpty()) {
                            drawBrushStroke(
                                PaintStroke(
                                    mode = PaintMode.BRUSH,
                                    points = currentPoints,
                                    color = activePaintColor,
                                    strokeWidth = brushSize
                                )
                            )
                        }
                    }
                    PaintMode.SPRAY -> {
                        if (currentSprayDots.isNotEmpty()) {
                            drawSprayDotsDirect(currentSprayDots, activePaintColor)
                        }
                    }
                    PaintMode.WATERCOLOR -> {
                        if (currentWatercolorDrops.isNotEmpty()) {
                            drawWatercolorDropsDirect(currentWatercolorDrops, activePaintColor)
                        }
                    }
                }

                // 3. Spray / Watercolor nozzle cursor ring when user is dragging
                activeTouchOffset?.let { touchPos ->
                    if (mode == PaintMode.SPRAY || mode == PaintMode.WATERCOLOR) {
                        drawCircle(
                            color = activePaintColor.copy(alpha = 0.35f),
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

            // 5. Initial prompt when no color is chosen yet and canvas is blank
            if (resolvedActiveColor == null && strokes.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x26FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✦",
                            color = Color(0xFFFFD54F),
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Choose your first mood color below",
                        color = Color(0xFFFFFFFF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Tap any color in the palette to start",
                        color = Color(0xFFB0B5C6),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Generates randomized aerosol / spray paint droplets around a central point.
 * Provides high particle count and larger droplet sizes for fast, bold surface coverage.
 */
private fun generateSprayDots(center: Offset, sprayDiameter: Float, count: Int): List<SprayDot> {
    val maxR = sprayDiameter / 2f
    val dots = ArrayList<SprayDot>(count)
    for (i in 0 until count) {
        val u = Random.nextFloat()
        val r = maxR * sqrt(u)
        val theta = Random.nextFloat() * (2f * Math.PI.toFloat())

        val x = center.x + r * cos(theta)
        val y = center.y + r * sin(theta)

        // Drop size ranges between 2.0px and 6.5px with central density
        val dotRadius = (2.0f + Random.nextFloat() * 4.5f)
        val falloff = (1f - (r / maxR).coerceIn(0f, 1f))
        val alpha = (0.30f + Random.nextFloat() * 0.70f) * (0.45f + falloff * 0.55f)

        dots.add(SprayDot(x = x, y = y, radius = dotRadius, alpha = alpha.coerceIn(0.15f, 1f)))
    }
    return dots
}

/**
 * Generates soft, feathered translucent watercolor drops with slight organic jitter.
 */
private fun generateWatercolorDrops(center: Offset, diameter: Float, count: Int): List<WatercolorDrop> {
    val drops = ArrayList<WatercolorDrop>(count)
    val baseRadius = diameter / 2f
    for (i in 0 until count) {
        val angle = Random.nextFloat() * (2f * Math.PI.toFloat())
        val dist = Random.nextFloat() * (baseRadius * 0.25f)
        val x = center.x + dist * cos(angle)
        val y = center.y + dist * sin(angle)
        val dropRadius = baseRadius * (0.85f + Random.nextFloat() * 0.30f)
        val alpha = (0.75f + Random.nextFloat() * 0.25f)
        drops.add(WatercolorDrop(x = x, y = y, radius = dropRadius, alpha = alpha))
    }
    return drops
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

/**
 * Draws soft bleeding watercolor pigment washes with radial gradients.
 */
private fun DrawScope.drawWatercolorStroke(stroke: PaintStroke) {
    drawWatercolorDropsDirect(stroke.watercolorDrops, stroke.color)
}

private fun DrawScope.drawWatercolorDropsDirect(drops: List<WatercolorDrop>, baseColor: Color) {
    for (drop in drops) {
        if (drop.radius <= 0f) continue
        val center = Offset(drop.x, drop.y)
        val centerAlpha = (baseColor.alpha * drop.alpha * 0.28f).coerceIn(0f, 1f)
        val midAlpha = (baseColor.alpha * drop.alpha * 0.10f).coerceIn(0f, 1f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    baseColor.copy(alpha = centerAlpha),
                    baseColor.copy(alpha = midAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = drop.radius
            ),
            radius = drop.radius,
            center = center
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
