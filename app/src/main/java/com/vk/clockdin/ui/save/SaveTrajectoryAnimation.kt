// Location: app/src/main/java/com/vk/clockdin/ui/save/SaveTrajectoryAnimation.kt
package com.vk.clockdin.ui.save

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vk.clockdin.domain.model.GradientStop
import java.io.File
import kotlin.math.roundToInt
import kotlin.random.Random

private data class TrajectoryWakeParticle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val initialAlpha: Float,
    val spawnTime: Float
)

/**
 * Fullscreen Cubic Bézier flight animation of the newly painted orb
 * from the paint canvas into the 3D galaxy sphere with a trailing cosmic stardust wake.
 */
@Composable
fun SaveTrajectoryAnimation(
    orbImagePath: String,
    dominantColors: List<GradientStop>,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    val wakeParticles = remember { mutableStateListOf<TrajectoryWakeParticle>() }
    val density = LocalDensity.current

    val primaryColor = remember(dominantColors) {
        dominantColors.firstOrNull()?.let { parseColor(it.hex) } ?: Color(0xFFFFC83D)
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1100,
                easing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
            )
        )
        onAnimationComplete()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Cubic Bézier control points
            val p0 = Offset(w * 0.5f, h * 0.42f) // Start (center of canvas)
            val p1 = Offset(w * 0.15f, h * 0.20f) // Control 1: swings up & left
            val p2 = Offset(w * 0.88f, h * 0.34f) // Control 2: loops gracefully
            val p3 = Offset(w * 0.50f, h * 0.50f) // Target: Galaxy center/bead

            val t = progress.value
            val currentPos = computeCubicBezier(p0, p1, p2, p3, t)

            // Spawn wake particles along the trajectory
            if (t < 0.95f && Random.nextFloat() > 0.3f) {
                wakeParticles.add(
                    TrajectoryWakeParticle(
                        x = currentPos.x + (Random.nextFloat() - 0.5f) * 20f,
                        y = currentPos.y + (Random.nextFloat() - 0.5f) * 20f,
                        radius = Random.nextFloat() * 3.5f + 1f,
                        color = primaryColor,
                        initialAlpha = 0.85f,
                        spawnTime = t
                    )
                )
            }

            // Draw fading wake particles
            val iterator = wakeParticles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                val age = t - p.spawnTime
                if (age > 0.30f) {
                    iterator.remove()
                } else {
                    val fade = (1f - (age / 0.30f)).coerceIn(0f, 1f)
                    drawCircle(
                        color = p.color.copy(alpha = p.initialAlpha * fade),
                        radius = p.radius,
                        center = Offset(p.x, p.y)
                    )
                }
            }

            // Landing cosmic pulse ring at the end of the trajectory
            if (t > 0.85f) {
                val burstProgress = ((t - 0.85f) / 0.15f).coerceIn(0f, 1f)
                val burstRadius = burstProgress * 65f
                val burstAlpha = (1f - burstProgress) * 0.8f
                drawCircle(
                    color = primaryColor.copy(alpha = burstAlpha),
                    radius = burstRadius,
                    center = p3,
                    style = Stroke(width = 2.dp.toPx() * (1f - burstProgress))
                )
            }

            // Ambient glow surrounding flying orb
            val orbScale = (1f - t * 0.92f).coerceIn(0.08f, 1f)
            val glowRadius = 140f * orbScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.55f * orbScale),
                        Color.Transparent
                    ),
                    center = currentPos,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = currentPos
            )
        }

        // Flying rendered Orb image
        val t = progress.value
        val orbBaseSize = 240.dp
        val orbScale = (1f - t * 0.92f).coerceIn(0.08f, 1f)

        // Compute current pixel offset
        val w = with(density) { 360.dp.toPx() } // Fallback width
        val h = with(density) { 720.dp.toPx() }

        val p0 = Offset(w * 0.5f, h * 0.42f)
        val p1 = Offset(w * 0.15f, h * 0.20f)
        val p2 = Offset(w * 0.88f, h * 0.34f)
        val p3 = Offset(w * 0.50f, h * 0.50f)
        val currentPos = computeCubicBezier(p0, p1, p2, p3, t)

        val orbSizePx = with(density) { orbBaseSize.toPx() }
        val offsetX = currentPos.x - (orbSizePx / 2f)
        val offsetY = currentPos.y - (orbSizePx / 2f)

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(orbBaseSize)
                .scale(orbScale)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = File(orbImagePath),
                contentDescription = "Flying Orb",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * Evaluates Cubic Bézier formula:
 * B(t) = (1-t)³P0 + 3(1-t)²tP1 + 3(1-t)t²P2 + t³P3
 */
private fun computeCubicBezier(p0: Offset, p1: Offset, p2: Offset, p3: Offset, t: Float): Offset {
    val u = 1f - t
    val tt = t * t
    val uu = u * u
    val uuu = uu * u
    val ttt = tt * t

    val x = uuu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + ttt * p3.x
    val y = uuu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + ttt * p3.y
    return Offset(x, y)
}

private fun parseColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val l = clean.toLong(16)
        if (clean.length == 6) Color(l or 0xFF000000) else Color(l)
    } catch (_: Exception) {
        Color(0xFFFFC83D)
    }
}
