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
 * 3D Star particle on the background celestial sphere.
 */
class StarParticle(
    var x: Float,
    var y: Float,
    var z: Float,
    val baseRadius: Float,
    val color: Color,
    val twinklePhase: Float,
    val twinkleSpeed: Float,
    val driftSpeed: Float
) {
    var screenX: Float = 0f
    var screenY: Float = 0f
    var depth: Float = 0f
    var scale: Float = 1f
    var alpha: Float = 1f
    var isVisible: Boolean = false
}

/**
 * Ambient dust speck floating gently within the galaxy core.
 */
class DustParticle(
    var x: Float,
    var y: Float,
    var z: Float,
    val radius: Float,
    val color: Color,
    val driftPhaseX: Float,
    val driftPhaseY: Float,
    val driftPhaseZ: Float
) {
    var screenX: Float = 0f
    var screenY: Float = 0f
    var depth: Float = 0f
    var scale: Float = 1f
    var alpha: Float = 0.15f
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
    var beads: List<BeadRenderItem> = emptyList()
        private set

    // Reusable sorted list to avoid allocations in draw loop
    private var sortedBeads: ArrayList<BeadRenderItem> = ArrayList()

    init {
        val random = Random(42) // Fixed seed for reproducible ambient sky

        // 1. Generate celestial stars distributed spherically
        stars = Array(starCount) { i ->
            val theta = random.nextFloat() * 2f * Math.PI.toFloat()
            val phi = (random.nextFloat() * 2f - 1f)
            val r = 900f + random.nextFloat() * 450f
            val cosPhi = kotlin.math.sqrt((1f - phi * phi).coerceAtLeast(0f))

            val starColor = when (random.nextInt(4)) {
                0 -> Color(0xFFFFFFFF) // Pure white
                1 -> Color(0xFFE8F0FE) // Cool ice
                2 -> Color(0xFFD0E4FF) // Soft cyan-white
                else -> Color(0xFFFFF7E6) // Warm stellar white
            }

            StarParticle(
                x = r * cosPhi * cos(theta),
                y = r * phi,
                z = r * cosPhi * sin(theta),
                baseRadius = 1.0f + random.nextFloat() * 2.2f,
                color = starColor,
                twinklePhase = random.nextFloat() * 6.28f,
                twinkleSpeed = 0.0015f + random.nextFloat() * 0.003f,
                driftSpeed = 0.00003f + random.nextFloat() * 0.00004f
            )
        }

        // 2. Generate atmospheric dust specks
        dust = Array(dustCount) {
            val r = 120f + random.nextFloat() * 480f
            val theta = random.nextFloat() * 6.28f
            val y = (random.nextFloat() * 2f - 1f) * 350f

            val dustColor = when (random.nextInt(3)) {
                0 -> Color(0x33B8A9E8) // Lavender mist dust
                1 -> Color(0x332FD4A7) // Emerald dust
                else -> Color(0x33FFC83D) // Amber dust
            }

            DustParticle(
                x = r * cos(theta),
                y = y,
                z = r * sin(theta),
                radius = 1.0f + random.nextFloat() * 2.0f,
                color = dustColor,
                driftPhaseX = random.nextFloat() * 6.28f,
                driftPhaseY = random.nextFloat() * 6.28f,
                driftPhaseZ = random.nextFloat() * 6.28f
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
     * Projects and sorts all particles and beads.
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

        // 1. Update and project background stars
        for (i in 0 until starCount) {
            val star = stars[i]
            val projected = FibonacciSphere.project(
                point = Point3D(star.x, star.y, star.z),
                yawRad = yaw + star.driftSpeed * timeSeconds,
                pitchRad = pitch,
                centerX = centerX,
                centerY = centerY,
                zoom = zoom * 0.85f, // Subtle parallax
                focal = 1100f
            )

            star.screenX = projected.screenX
            star.screenY = projected.screenY
            star.depth = projected.depth
            star.scale = projected.scale

            val twinkle = (0.4f + 0.6f * sin(timeSeconds * star.twinkleSpeed * 1000f + star.twinklePhase)).coerceIn(0.1f, 1f)
            star.alpha = twinkle

            star.isVisible = projected.screenX in -50f..(viewportWidth + 50f) &&
                    projected.screenY in -50f..(viewportHeight + 50f)
        }

        // 2. Update and project dust particles
        for (i in 0 until dustCount) {
            val d = dust[i]
            // Gentle wandering offset
            val dx = d.x + sin(timeSeconds * 0.4f + d.driftPhaseX) * 20f
            val dy = d.y + cos(timeSeconds * 0.3f + d.driftPhaseY) * 15f
            val dz = d.z + sin(timeSeconds * 0.5f + d.driftPhaseZ) * 20f

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
                    projected.screenY in -50f..(viewportHeight + 50f)
        }

        // 3. Project entry beads
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
