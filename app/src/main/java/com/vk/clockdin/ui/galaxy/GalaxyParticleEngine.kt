// Location: app/src/main/java/com/vk/clockdin/ui/galaxy/GalaxyParticleEngine.kt
package com.vk.clockdin.ui.galaxy

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.vk.clockdin.domain.math.FibonacciSphere
import com.vk.clockdin.domain.math.Point3D
import com.vk.clockdin.domain.model.MoodEntry
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 3D Star particle traveling through space toward the camera (infinite cosmic fly-through).
 */
class StarParticle(
    val initialX: Float,
    val initialY: Float,
    val initialZ: Float,
    val baseRadius: Float,
    val color: Color,
    val twinklePhase: Float,
    val twinkleSpeed: Float,
    val warpSpeed: Float,
    val driftVx: Float
) {
    var screenX: Float = 0f
    var screenY: Float = 0f
    var depth: Float = 0f
    var scale: Float = 1f
    var alpha: Float = 1f
    var isVisible: Boolean = false
}

/**
 * Ambient dust speck floating and drifting across the camera viewport.
 */
class DustParticle(
    val initialX: Float,
    val initialY: Float,
    val initialZ: Float,
    val radius: Float,
    val color: Color,
    val driftVx: Float,
    val driftVy: Float,
    val driftVz: Float,
    val driftPhaseX: Float,
    val driftPhaseY: Float
) {
    var screenX: Float = 0f
    var screenY: Float = 0f
    var depth: Float = 0f
    var scale: Float = 1f
    var alpha: Float = 0.55f
    var isVisible: Boolean = false
}

/**
 * Volumetric 3D cosmic nebula (space cloud) continuously drifting across and through the screen.
 */
class NebulaCloud(
    val initialX: Float,
    val initialY: Float,
    val initialZ: Float,
    val speedX: Float,
    val speedY: Float,
    val speedZ: Float,
    val baseRadius: Float,
    val innerColor: Color,
    val outerColor: Color
) {
    var screenX: Float = 0f
    var screenY: Float = 0f
    var depth: Float = 0f
    var scale: Float = 1f
    var radius: Float = 300f
    var alpha: Float = 1f
    var isVisible: Boolean = false
}

/**
 * High-performance container for an entry bead projected in 3D.
 */
class BeadRenderItem(
    val entry: MoodEntry,
    val worldPos: Point3D,
    val dominantColor: Color,
    var cachedBitmap: ImageBitmap? = null
) {
    var screenX: Float = 0f
    var screenY: Float = 0f
    var depth: Float = 0f
    var scale: Float = 1f
    var visualRadius: Float = 24f
    var glowRadius: Float = 60f
    var alpha: Float = 1f
    var isVisible: Boolean = false
}

/**
 * Custom 3D particle and bead engine for the Galaxy View.
 * Pre-allocates data structures to ensure ZERO allocations in the render/draw loop.
 */
class GalaxyParticleEngine(
    val starCount: Int = 180,
    val dustCount: Int = 60
) {
    val stars: Array<StarParticle>
    val dust: Array<DustParticle>
    val nebulae: Array<NebulaCloud>
    var beads: List<BeadRenderItem> = emptyList()
        private set

    // Reusable sorted list to avoid allocations in draw loop
    private var sortedBeads: ArrayList<BeadRenderItem> = ArrayList()

    init {
        val random = Random(42) // Fixed seed for reproducible ambient sky

        // 1. Volumetric 3D cosmic nebulae (space clouds) drifting through the galaxy
        nebulae = arrayOf(
            NebulaCloud(
                initialX = -1200f, initialY = -350f, initialZ = -100f,
                speedX = 38f, speedY = 14f, speedZ = 8f,
                baseRadius = 380f,
                innerColor = Color(0x487B1FA2), // Violet nebula
                outerColor = Color.Transparent
            ),
            NebulaCloud(
                initialX = 800f, initialY = 400f, initialZ = 50f,
                speedX = -32f, speedY = -18f, speedZ = -10f,
                baseRadius = 340f,
                innerColor = Color(0x4200B4D8), // Vibrant cyan space cloud
                outerColor = Color.Transparent
            ),
            NebulaCloud(
                initialX = -400f, initialY = 900f, initialZ = -150f,
                speedX = 25f, speedY = -34f, speedZ = 12f,
                baseRadius = 350f,
                innerColor = Color(0x3FFF8F00), // Amber solar flare cloud
                outerColor = Color.Transparent
            ),
            NebulaCloud(
                initialX = 1100f, initialY = -500f, initialZ = 120f,
                speedX = -36f, speedY = 20f, speedZ = -6f,
                baseRadius = 360f,
                innerColor = Color(0x463F51B5), // Deep cobalt galaxy dust
                outerColor = Color.Transparent
            ),
            NebulaCloud(
                initialX = -700f, initialY = -800f, initialZ = -80f,
                speedX = 30f, speedY = 28f, speedZ = 14f,
                baseRadius = 330f,
                innerColor = Color(0x40FF4081), // Magenta astral cloud
                outerColor = Color.Transparent
            ),
            NebulaCloud(
                initialX = 300f, initialY = 1100f, initialZ = 80f,
                speedX = -24f, speedY = -28f, speedZ = -8f,
                baseRadius = 320f,
                innerColor = Color(0x3E00897B), // Emerald cosmic mist
                outerColor = Color.Transparent
            )
        )

        // 2. Generate celestial stars traveling forward through cosmic depth
        stars = Array(starCount) { i ->
            val theta = random.nextFloat() * 2f * Math.PI.toFloat()
            val r = 250f + random.nextFloat() * 850f

            val starColor = when (random.nextInt(4)) {
                0 -> Color(0xFFFFFFFF) // Pure white
                1 -> Color(0xFFE8F0FE) // Cool ice
                2 -> Color(0xFFD0E4FF) // Soft cyan-white
                else -> Color(0xFFFFF7E6) // Warm stellar white
            }

            StarParticle(
                initialX = r * cos(theta),
                initialY = (random.nextFloat() * 2f - 1f) * 900f,
                initialZ = (random.nextFloat() * 2f - 1f) * 1100f,
                baseRadius = 1.0f + random.nextFloat() * 2.2f,
                color = starColor,
                twinklePhase = random.nextFloat() * 6.28f,
                twinkleSpeed = 0.002f + random.nextFloat() * 0.004f,
                warpSpeed = 55f + random.nextFloat() * 85f,
                driftVx = (random.nextFloat() * 2f - 1f) * 15f
            )
        }

        // 3. Generate atmospheric dust specks drifting across the viewport
        dust = Array(dustCount) {
            val x = (random.nextFloat() * 2f - 1f) * 800f
            val y = (random.nextFloat() * 2f - 1f) * 700f
            val z = (random.nextFloat() * 2f - 1f) * 600f

            val dustColor = when (random.nextInt(4)) {
                0 -> Color(0x66B8A9E8) // Lavender mist dust
                1 -> Color(0x662FD4A7) // Emerald dust
                2 -> Color(0x66FFC83D) // Amber dust
                else -> Color(0x6600E5FF) // Cyan astral dust
            }

            DustParticle(
                initialX = x,
                initialY = y,
                initialZ = z,
                radius = 1.2f + random.nextFloat() * 2.5f,
                color = dustColor,
                driftVx = 25f + random.nextFloat() * 45f,
                driftVy = 12f + random.nextFloat() * 30f,
                driftVz = 15f + random.nextFloat() * 35f,
                driftPhaseX = random.nextFloat() * 6.28f,
                driftPhaseY = random.nextFloat() * 6.28f
            )
        }
    }

    /**
     * Updates the bead items from entries list using the deterministic Fibonacci sphere layout.
     * Oldest entries are centrally placed, expanding outward for newer entries.
     */
    fun updateEntries(entries: List<MoodEntry>) {
        val total = entries.size
        val newBeads = ArrayList<BeadRenderItem>(total)

        for (i in 0 until total) {
            val entry = entries[i]
            val worldPos = FibonacciSphere.calculatePosition(
                index = i,
                totalCount = total,
                maxRadius = 420f,
                minRadius = 80f
            )

            // Derive dominant color from gradient stops
            val dominantColor = parseDominantColor(entry)

            newBeads.add(
                BeadRenderItem(
                    entry = entry,
                    worldPos = worldPos,
                    dominantColor = dominantColor
                )
            )
        }

        beads = newBeads
        sortedBeads = ArrayList(newBeads)
    }

    /**
     * Projects and sorts all particles, space clouds, and beads.
     * Runs every frame with ZERO object allocations.
     */
    fun projectAndSort(
        yaw: Float,
        pitch: Float,
        centerX: Float,
        centerY: Float,
        zoom: Float,
        viewportWidth: Float,
        viewportHeight: Float,
        timeNanos: Long
    ): List<BeadRenderItem> {
        val timeSeconds = timeNanos / 1_000_000_000f

        // 1. Volumetric 3D cosmic space clouds (nebulae) drifting through screen
        val spanX = 2600f
        val spanY = 2200f
        val spanZ = 1600f

        for (i in nebulae.indices) {
            val nebula = nebulae[i]
            val rawNx = nebula.initialX + timeSeconds * nebula.speedX
            val nx = (((rawNx + spanX / 2f) % spanX + spanX) % spanX) - spanX / 2f

            val rawNy = nebula.initialY + timeSeconds * nebula.speedY
            val ny = (((rawNy + spanY / 2f) % spanY + spanY) % spanY) - spanY / 2f

            val rawNz = nebula.initialZ + timeSeconds * nebula.speedZ
            val nz = (((rawNz + spanZ / 2f) % spanZ + spanZ) % spanZ) - spanZ / 2f

            val dynamicRadius = nebula.baseRadius * (1f + 0.16f * sin(timeSeconds * 0.35f + i * 1.2f))

            // Smooth boundary fading so clouds fade in as they enter and fade out as they leave
            val edgeFadeX = (1f - (kotlin.math.abs(nx) / (spanX * 0.48f))).coerceIn(0f, 1f)
            val edgeFadeY = (1f - (kotlin.math.abs(ny) / (spanY * 0.48f))).coerceIn(0f, 1f)
            nebula.alpha = edgeFadeX * edgeFadeY

            val projected = FibonacciSphere.project(
                point = Point3D(nx, ny, nz),
                yawRad = yaw * 0.5f,
                pitchRad = pitch * 0.5f,
                centerX = centerX,
                centerY = centerY,
                zoom = zoom * 0.9f,
                focal = 1100f
            )

            nebula.screenX = projected.screenX
            nebula.screenY = projected.screenY
            nebula.depth = projected.depth
            nebula.scale = projected.scale
            nebula.radius = dynamicRadius * projected.scale

            nebula.isVisible = projected.screenX in (-nebula.radius)..(viewportWidth + nebula.radius) &&
                    projected.screenY in (-nebula.radius)..(viewportHeight + nebula.radius) &&
                    nebula.alpha > 0.01f
        }

        // 2. Update and project background stars traveling forward (cosmic fly-through)
        val starZSpan = 2200f
        for (i in 0 until starCount) {
            val star = stars[i]
            val rawZ = star.initialZ - timeSeconds * star.warpSpeed
            val curZ = (((rawZ + 1400f) % starZSpan + starZSpan) % starZSpan) - 800f

            // Continuous harmonic drift without periodic sawtooth jump
            val curX = star.initialX + sin(timeSeconds * 0.14f + star.twinklePhase) * 40f + star.driftVx * 12f
            val curY = star.initialY + cos(timeSeconds * 0.10f + star.twinklePhase) * 28f

            val projected = FibonacciSphere.project(
                point = Point3D(curX, curY, curZ),
                yawRad = yaw * 0.85f,
                pitchRad = pitch * 0.85f,
                centerX = centerX,
                centerY = centerY,
                zoom = zoom,
                focal = 1100f
            )

            star.screenX = projected.screenX
            star.screenY = projected.screenY
            star.depth = projected.depth
            star.scale = projected.scale

            // Smooth depth fading at wrap horizons: fades in far away, fades out near camera
            val nearFade = ((curZ - (-800f)) / 300f).coerceIn(0f, 1f)
            val farFade = ((1400f - curZ) / 350f).coerceIn(0f, 1f)
            val depthAlpha = nearFade * farFade

            // Fluid, gentle celestial shimmer
            val twinkle = (0.35f + 0.65f * sin(timeSeconds * (1.2f + star.twinkleSpeed * 2.0f) + star.twinklePhase)).coerceIn(0.15f, 1f)
            star.alpha = twinkle * depthAlpha

            star.isVisible = projected.screenX in -50f..(viewportWidth + 50f) &&
                    projected.screenY in -50f..(viewportHeight + 50f) &&
                    star.alpha > 0.02f
        }

        // 3. Update and project dust particles drifting across screen
        val dSpanX = 2000f
        val dSpanY = 1800f
        val dSpanZ = 1600f
        for (i in 0 until dustCount) {
            val d = dust[i]
            val rawDx = d.initialX + timeSeconds * d.driftVx
            val dx = (((rawDx + dSpanX / 2f) % dSpanX + dSpanX) % dSpanX) - dSpanX / 2f + sin(timeSeconds * 0.4f + d.driftPhaseX) * 35f

            val rawDy = d.initialY + timeSeconds * d.driftVy
            val dy = (((rawDy + dSpanY / 2f) % dSpanY + dSpanY) % dSpanY) - dSpanY / 2f + cos(timeSeconds * 0.35f + d.driftPhaseY) * 30f

            val rawDz = d.initialZ + timeSeconds * d.driftVz
            val dz = (((rawDz + dSpanZ / 2f) % dSpanZ + dSpanZ) % dSpanZ) - dSpanZ / 2f

            // Smooth edge and depth fade
            val nearFade = ((dz - (-800f)) / 250f).coerceIn(0f, 1f)
            val farFade = ((800f - dz) / 250f).coerceIn(0f, 1f)
            val fadeX = (1f - (kotlin.math.abs(dx) / (dSpanX * 0.48f))).coerceIn(0f, 1f)
            val fadeY = (1f - (kotlin.math.abs(dy) / (dSpanY * 0.48f))).coerceIn(0f, 1f)
            d.alpha = (nearFade * farFade * fadeX * fadeY).coerceIn(0f, 1f) * 0.75f

            val projected = FibonacciSphere.project(
                point = Point3D(dx, dy, dz),
                yawRad = yaw,
                pitchRad = pitch,
                centerX = centerX,
                centerY = centerY,
                zoom = zoom,
                focal = 1100f
            )

            d.screenX = projected.screenX
            d.screenY = projected.screenY
            d.depth = projected.depth
            d.scale = projected.scale

            d.isVisible = projected.screenX in -50f..(viewportWidth + 50f) &&
                    projected.screenY in -50f..(viewportHeight + 50f) &&
                    d.alpha > 0.02f
        }

        // 4. Project entry beads
        for (i in beads.indices) {
            val bead = beads[i]
            val projected = FibonacciSphere.project(
                point = bead.worldPos,
                yawRad = yaw,
                pitchRad = pitch,
                centerX = centerX,
                centerY = centerY,
                zoom = zoom,
                focal = 1100f
            )

            bead.screenX = projected.screenX
            bead.screenY = projected.screenY
            bead.depth = projected.depth
            bead.scale = projected.scale

            // Base radius scales with depth perspective: 18dp to 38dp
            bead.visualRadius = (22f * projected.scale).coerceIn(10f, 65f)
            bead.glowRadius = bead.visualRadius * 2.8f

            // Depth alpha falloff: closer beads are fully opaque, distant beads fade slightly
            bead.alpha = ((projected.scale - 0.4f) / 0.8f).coerceIn(0.35f, 1.0f)

            bead.isVisible = projected.screenX in (-bead.glowRadius)..(viewportWidth + bead.glowRadius) &&
                    projected.screenY in (-bead.glowRadius)..(viewportHeight + bead.glowRadius)
        }

        // Sort beads by depth (far -> near: ascending depth z2)
        sortedBeads.sortWith { a, b ->
            a.depth.compareTo(b.depth)
        }

        return sortedBeads
    }

    /**
     * Hit tests nearest bead within radius tolerance of a screen tap.
     * Nearest-front (highest depth / scale) wins.
     */
    fun hitTest(tapX: Float, tapY: Float, tolerancePx: Float = 75f): MoodEntry? {
        var closestBead: BeadRenderItem? = null
        var maxDepth = Float.NEGATIVE_INFINITY

        for (i in beads.indices) {
            val bead = beads[i]
            if (!bead.isVisible) continue

            val dx = tapX - bead.screenX
            val dy = tapY - bead.screenY
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)

            val effectiveTolerance = (bead.visualRadius * 1.5f).coerceAtLeast(tolerancePx)
            if (dist <= effectiveTolerance) {
                // If multiple beads overlap under tap point, prefer the one closer to camera
                if (bead.depth > maxDepth) {
                    maxDepth = bead.depth
                    closestBead = bead
                }
            }
        }

        return closestBead?.entry
    }

    private fun parseDominantColor(entry: MoodEntry): Color {
        val hex = entry.gradientStops.firstOrNull()?.hex ?: "#FFC83D"
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorLong = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(colorLong or 0xFF000000)
            } else {
                Color(colorLong)
            }
        } catch (_: Exception) {
            Color(0xFFFFC83D)
        }
    }
}
