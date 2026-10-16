// Location: app/src/main/java/com/vk/clockdin/ui/galaxy/GalaxyHomeScreen.kt
package com.vk.clockdin.ui.galaxy

import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vk.clockdin.domain.math.FibonacciSphere
import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.ui.theme.spaceBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Galaxy Home signature screen:
 * Full-screen 3D particle canvas with stars, atmospheric dust, and entry beads
 * positioned on a deterministic Fibonacci sphere with perspective projection,
 * AGSL glow halos, fling inertia, tap-to-zoom flight animation, and hit-testing.
 */
@Composable
fun GalaxyHomeScreen(
    onNavigateToPaint: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToView: (String) -> Unit,
    onOpenDetail: (Long) -> Unit,
    newlySavedOrb: Pair<String, List<GradientStop>>? = null,
    onSaveAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GalaxyHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Handle ViewModel single-shot events
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is GalaxyEvent.OpenDetail -> onOpenDetail(event.entryId)
                is GalaxyEvent.OpenPaint -> onNavigateToPaint()
                is GalaxyEvent.OpenSettings -> onNavigateToSettings()
                is GalaxyEvent.SwitchView -> onNavigateToView(event.route)
                is GalaxyEvent.PerformHaptic -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .spaceBackground(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fullscreen 3D Galaxy Canvas with continuous cosmic orbit and direct orb flight
            GalaxyCanvas(
                entries = uiState.entries,
                onBeadTap = { entryId -> viewModel.onBeadTapped(entryId) },
                newlySavedOrb = newlySavedOrb,
                onSaveAnimationComplete = onSaveAnimationComplete,
                modifier = Modifier.fillMaxSize()
            )

            // Minimal Glass Header & Weekly Mood Percentage Breakdown
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GalaxyTopBar(
                    streakCount = uiState.streakCount,
                    currentPerspective = GalaxyPerspective.GALAXY,
                    onPerspectiveSelected = { perspective ->
                        viewModel.onViewSelected(perspective.route)
                    },
                    onSettingsClick = { viewModel.onSettingsClicked() },
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.weeklyStats.totalEntriesThisWeek > 0 && uiState.weeklyStats.majorityMood != null) {
                    WeeklyMoodPercentageCard(
                        stats = uiState.weeklyStats,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                }
            }

            // Glowing Floating Action Button "+"
            GalaxyFab(
                onClick = { viewModel.onFabClicked() },
                hasEntryToday = uiState.hasEntryToday,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 28.dp)
            )
        }
    }
}

@Composable
fun GalaxyCanvas(
    entries: List<MoodEntry>,
    onBeadTap: (Long) -> Unit,
    newlySavedOrb: Pair<String, List<GradientStop>>? = null,
    onSaveAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val engine = remember { GalaxyParticleEngine() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Camera dynamic state
    var yaw by remember { mutableFloatStateOf(0.4f) }
    var pitch by remember { mutableFloatStateOf(0.15f) }
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var isCameraFlying by remember { mutableStateOf(false) }

    // Velocity tracker and inertia animation
    val velocityTracker = remember { VelocityTracker() }
    val inertiaAnim = remember { Animatable(0f) }

    // In-flight save trajectory state
    val flightProgress = remember { Animatable(1f) }
    var activeSavedOrb by remember { mutableStateOf<Pair<String, List<GradientStop>>?>(null) }
    var flightBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(newlySavedOrb) {
        if (newlySavedOrb != null) {
            activeSavedOrb = newlySavedOrb
            flightBitmap = withContext(Dispatchers.IO) {
                val file = File(newlySavedOrb.first)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
            }
            flightProgress.snapTo(0f)
            flightProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(850, easing = CubicBezierEasing(0.20f, 0.85f, 0.30f, 1.0f))
            )
            onSaveAnimationComplete()
            activeSavedOrb = null
        }
    }

    // Frame clock driven by withFrameNanos (zero allocations)
    var frameTimeNanos by remember { mutableLongStateOf(0L) }
    var lastInteractionTime by remember { mutableLongStateOf(0L) }
    var lastNanos by remember { mutableLongStateOf(0L) }

    // Update entries in engine and load rendered PNG orbs asynchronously
    LaunchedEffect(entries) {
        engine.updateEntries(entries)

        withContext(Dispatchers.IO) {
            for (bead in engine.beads) {
                if (bead.cachedBitmap == null && bead.entry.orbImagePath.isNotBlank()) {
                    val file = File(bead.entry.orbImagePath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        bead.cachedBitmap = bitmap?.asImageBitmap()
                    }
                }
            }
        }
    }

    // Continuous 60/120fps animation clock with fluid celestial auto-rotation
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { nanos ->
                frameTimeNanos = nanos
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = nanos

                val now = System.currentTimeMillis()
                val isIdle = (now - lastInteractionTime) > 600L && !isCameraFlying && !inertiaAnim.isRunning
                if (isIdle) {
                    // Constant, majestic 3D celestial auto-rotation (~12.6 degrees per second)
                    yaw += 0.22f * dt

                    // Gentle return to standard zoom 1.0f if left zoomed in
                    if (kotlin.math.abs(zoom - 1.0f) > 0.02f) {
                        zoom += (1.0f - zoom) * 2.5f * dt
                    }
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (isCameraFlying) return@detectTapGestures
                    val tappedEntry = engine.hitTest(offset.x, offset.y, tolerancePx = 65f)
                    if (tappedEntry != null) {
                        val bead = engine.beads.firstOrNull { it.entry.id == tappedEntry.id }
                        if (bead != null) {
                            coroutineScope.launch {
                                isCameraFlying = true
                                lastInteractionTime = System.currentTimeMillis()

                                // Calculate spherical angles to fly and center camera right on this orb
                                val targetYaw = -kotlin.math.atan2(bead.worldPos.x, bead.worldPos.z)
                                val diffYaw = (targetYaw - yaw) % (2f * Math.PI.toFloat())
                                val normDiffYaw = when {
                                    diffYaw > Math.PI.toFloat() -> diffYaw - (2f * Math.PI.toFloat())
                                    diffYaw < -Math.PI.toFloat() -> diffYaw + (2f * Math.PI.toFloat())
                                    else -> diffYaw
                                }
                                val flyYaw = yaw + normDiffYaw

                                val hypXZ = kotlin.math.sqrt(bead.worldPos.x * bead.worldPos.x + bead.worldPos.z * bead.worldPos.z)
                                val targetPitch = kotlin.math.atan2(bead.worldPos.y, hypXZ).coerceIn(-1.25f, 1.25f)
                                val targetZoom = 2.0f

                                val animYaw = Animatable(yaw)
                                val animPitch = Animatable(pitch)
                                val animZoom = Animatable(zoom)

                                val j1 = launch {
                                    animYaw.animateTo(flyYaw, tween(360, easing = FastOutSlowInEasing)) {
                                        yaw = value
                                    }
                                }
                                val j2 = launch {
                                    animPitch.animateTo(targetPitch, tween(360, easing = FastOutSlowInEasing)) {
                                        pitch = value
                                    }
                                }
                                val j3 = launch {
                                    animZoom.animateTo(targetZoom, tween(360, easing = FastOutSlowInEasing)) {
                                        zoom = value
                                    }
                                }

                                joinAll(j1, j2, j3)
                                onBeadTap(tappedEntry.id)
                                delay(300L)
                                isCameraFlying = false
                            }
                        } else {
                            onBeadTap(tappedEntry.id)
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { centroid, pan, zoomFactor, _ ->
                        if (isCameraFlying) return@detectTransformGestures
                        lastInteractionTime = System.currentTimeMillis()
                        yaw += pan.x * 0.004f
                        pitch = (pitch - pan.y * 0.004f).coerceIn(-1.396f, 1.396f) // Clamped +/- 80 deg
                        zoom = (zoom * zoomFactor).coerceIn(0.5f, 3.0f)

                        velocityTracker.addPosition(
                            System.currentTimeMillis(),
                            Offset(pan.x, pan.y)
                        )

                        // Trigger inertia decay on gesture finish
                        coroutineScope.launch {
                            val velocity = velocityTracker.calculateVelocity()
                            val vx = velocity.x
                            if (kotlin.math.abs(vx) > 300f) {
                                inertiaAnim.snapTo(yaw)
                                inertiaAnim.animateDecay(
                                    initialVelocity = vx * 0.0008f,
                                    animationSpec = exponentialDecay(frictionMultiplier = 1.2f)
                                ) {
                                    yaw = value
                                }
                            }
                        }
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f

        // Project and sort particles and beads with zero allocation
        val sortedBeads = engine.projectAndSort(
            yaw = yaw,
            pitch = pitch,
            centerX = centerX,
            centerY = centerY,
            zoom = zoom,
            viewportWidth = width,
            viewportHeight = height,
            timeNanos = frameTimeNanos
        )

        // 1. Draw volumetric 3D space clouds (cosmic nebulae)
        val nebulae = engine.nebulae
        for (i in nebulae.indices) {
            val n = nebulae[i]
            if (!n.isVisible) continue

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        n.innerColor.copy(alpha = n.innerColor.alpha * n.alpha),
                        n.outerColor
                    ),
                    center = Offset(n.screenX, n.screenY),
                    radius = n.radius
                ),
                radius = n.radius,
                center = Offset(n.screenX, n.screenY)
            )
        }

        // 2. Draw celestial stars
        val stars = engine.stars
        for (i in stars.indices) {
            val star = stars[i]
            if (!star.isVisible) continue

            drawCircle(
                color = star.color.copy(alpha = star.alpha),
                radius = star.baseRadius * star.scale,
                center = Offset(star.screenX, star.screenY)
            )
        }

        // 3. Draw atmospheric dust particles
        val dust = engine.dust
        for (i in dust.indices) {
            val d = dust[i]
            if (!d.isVisible) continue

            drawCircle(
                color = d.color.copy(alpha = d.color.alpha * d.alpha),
                radius = d.radius * d.scale,
                center = Offset(d.screenX, d.screenY)
            )
        }

        val inFlightOrb = activeSavedOrb
        val targetBead = inFlightOrb?.let { orb ->
            engine.beads.firstOrNull { it.entry.orbImagePath == orb.first }
        }

        // 4. Draw beads (capped to nearest 400 for full glow, rest as simple dots)
        val beadCount = sortedBeads.size
        val fullGlowThreshold = (beadCount - 400).coerceAtLeast(0)

        for (i in 0 until beadCount) {
            val bead = sortedBeads[i]
            if (!bead.isVisible) continue

            // If this bead is actively in-flight arriving from the paint canvas, suppress static drawing
            if (inFlightOrb != null && (bead == targetBead || bead.entry.orbImagePath == inFlightOrb.first) && flightProgress.value < 0.98f) {
                continue
            }

            val centerOffset = Offset(bead.screenX, bead.screenY)

            if (i >= fullGlowThreshold) {
                // AGSL RuntimeShader radial neon/pastel glow halo
                val glowBrush = GalaxyShader.createGlowBrush(
                    centerX = bead.screenX,
                    centerY = bead.screenY,
                    radius = bead.glowRadius,
                    color = bead.dominantColor,
                    intensity = 0.85f * bead.alpha
                )

                drawCircle(
                    brush = glowBrush,
                    radius = bead.glowRadius,
                    center = centerOffset
                )

                // Render circular orb bitmap or radial gradient fallback
                val bitmap = bead.cachedBitmap
                if (bitmap != null) {
                    val diameter = (bead.visualRadius * 2f).toInt().coerceAtLeast(2)
                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(
                            (bead.screenX - bead.visualRadius).toInt(),
                            (bead.screenY - bead.visualRadius).toInt()
                        ),
                        dstSize = IntSize(diameter, diameter),
                        alpha = bead.alpha
                    )
                } else {
                    // Fallback smooth radial gradient using dominant colors
                    val orbBrush = Brush.radialGradient(
                        colors = listOf(
                            bead.dominantColor.copy(alpha = bead.alpha),
                            bead.dominantColor.copy(alpha = bead.alpha * 0.4f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = bead.visualRadius
                    )
                    drawCircle(
                        brush = orbBrush,
                        radius = bead.visualRadius,
                        center = centerOffset
                    )
                }
            } else {
                // Distant compact star-bead
                drawCircle(
                    color = bead.dominantColor.copy(alpha = bead.alpha),
                    radius = bead.visualRadius,
                    center = centerOffset
                )
            }
        }

        // 5. Draw in-flight newly saved orb gliding smoothly from screen center into its exact galaxy position
        if (inFlightOrb != null) {
            val t = flightProgress.value
            val targetIndex = targetBead?.let { engine.beads.indexOf(it) } ?: engine.beads.size
            val totalCount = (if (targetBead != null) engine.beads.size else engine.beads.size + 1).coerceAtLeast(1)
            val targetWorldPos = FibonacciSphere.calculatePosition(
                index = targetIndex,
                totalCount = totalCount,
                sphereRadius = 360f
            )
            val targetProjected = FibonacciSphere.project(
                point = targetWorldPos,
                yawRad = yaw,
                pitchRad = pitch,
                centerX = centerX,
                centerY = centerY,
                zoom = zoom,
                focal = 1100f
            )
            val targetVisualRadius = (22f * targetProjected.scale).coerceIn(10f, 65f)

            val p0 = Offset(centerX, centerY)
            val p3 = Offset(targetProjected.screenX, targetProjected.screenY)
            val p1 = Offset(p0.x + (p3.x - p0.x) * 0.35f, p0.y + (p3.y - p0.y) * 0.15f - 30f)
            val p2 = Offset(p0.x + (p3.x - p0.x) * 0.70f, p3.y - 15f)
            val currentPos = computeCubicBezier(p0, p1, p2, p3, t)

            val startRadius = with(density) { 38.dp.toPx() }
            val currentRadius = startRadius + (targetVisualRadius - startRadius) * t

            val primaryColor = inFlightOrb.second.firstOrNull()?.let { parseDominantHex(it.hex) } ?: targetBead?.dominantColor ?: Color(0xFFB388FF)

            // Subtle glowing halo wake
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.45f * (1f - t * 0.25f)),
                        Color.Transparent
                    ),
                    center = currentPos,
                    radius = currentRadius * 1.35f
                ),
                radius = currentRadius * 1.35f,
                center = currentPos
            )

            // Render in-flight orb image
            val bmp = flightBitmap ?: targetBead?.cachedBitmap
            if (bmp != null) {
                val diameter = (currentRadius * 2f).toInt().coerceAtLeast(2)
                drawImage(
                    image = bmp,
                    dstOffset = IntOffset(
                        (currentPos.x - currentRadius).toInt(),
                        (currentPos.y - currentRadius).toInt()
                    ),
                    dstSize = IntSize(diameter, diameter),
                    alpha = 1f
                )
            } else {
                drawCircle(
                    color = primaryColor,
                    radius = currentRadius,
                    center = currentPos
                )
            }

            // Landing subtle shockwave ring when t approaches 1f
            if (t > 0.80f) {
                val burstT = ((t - 0.80f) / 0.20f).coerceIn(0f, 1f)
                val burstRadius = targetVisualRadius + burstT * with(density) { 22.dp.toPx() }
                val burstAlpha = (1f - burstT) * 0.75f
                drawCircle(
                    color = primaryColor.copy(alpha = burstAlpha),
                    radius = burstRadius,
                    center = p3,
                    style = Stroke(width = with(density) { 2.dp.toPx() } * (1f - burstT))
                )
            }
        }
    }
}

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

private fun parseDominantHex(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val l = clean.toLong(16)
        if (clean.length == 6) Color(l or 0xFF000000) else Color(l)
    } catch (_: Exception) {
        Color(0xFFFFC83D)
    }
}
