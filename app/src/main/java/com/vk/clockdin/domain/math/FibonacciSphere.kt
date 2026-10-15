// Location: app/src/main/java/com/vk/clockdin/domain/math/FibonacciSphere.kt
package com.vk.clockdin.domain.math

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 3D point in world space.
 */
data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * 2D projected point on screen with depth metadata for Z-sorting and scaling.
 */
data class ProjectedPoint(
    val screenX: Float,
    val screenY: Float,
    val depth: Float,
    val scale: Float
)

/**
 * Mathematical utilities for deterministic 3D Fibonacci spiral/sphere positioning
 * and perspective projection.
 */
object FibonacciSphere {

    /**
     * Golden angle in radians: PI * (3.0 - sqrt(5.0)) ~ 2.3999632 radians (approx 137.5 degrees).
     */
    val GOLDEN_ANGLE: Double = Math.PI * (3.0 - sqrt(5.0))

    /**
     * Calculates the 3D position of an orb bead placed on a deterministic Fibonacci spiral/sphere.
     * Oldest entries (low index) are positioned centrally/innermost, expanding outward for newer entries.
     *
     * Spherical -> Cartesian mapping:
     * x = r * cos(lat) * cos(lon)
     * y = r * sin(lat)
     * z = r * cos(lat) * sin(lon)
     *
     * @param index 0-based index of the entry (0 = oldest).
     * @param totalCount Total count of entries in the galaxy.
     * @param maxRadius Outer sphere radius in world units.
     * @param minRadius Inner radius for the first entry.
     */
    fun calculatePosition(
        index: Int,
        totalCount: Int,
        maxRadius: Float = 420f,
        minRadius: Float = 80f
    ): Point3D {
        if (totalCount <= 1) {
            return Point3D(0f, 0f, 0f)
        }

        // Fraction of progress from oldest to newest: 0.0 .. 1.0
        val progress = (index.toDouble() / (totalCount - 1).toDouble()).coerceIn(0.0, 1.0)

        // Radial expansion: oldest at center/minRadius -> newest outward toward maxRadius
        val r = (minRadius + (maxRadius - minRadius) * sqrt(progress)).toFloat()

        // Distribute latitude evenly from South to North poles: -PI/2 .. +PI/2
        // yFraction spans from -1.0 to +1.0
        val yFraction = (2.0 * (index + 0.5) / totalCount) - 1.0
        val clampedY = yFraction.coerceIn(-1.0, 1.0)
        val lat = asin(clampedY)

        // Longitude steps by the golden angle
        val lon = index * GOLDEN_ANGLE

        val cosLat = cos(lat)
        val sinLat = sin(lat)
        val cosLon = cos(lon)
        val sinLon = sin(lon)

        val x = (r * cosLat * cosLon).toFloat()
        val y = (r * sinLat).toFloat()
        val z = (r * cosLat * sinLon).toFloat()

        return Point3D(x, y, z)
    }

    /**
     * Rotates a 3D point by yaw (around Y-axis) then pitch (around X-axis),
     * and projects onto 2D screen coordinates with perspective division.
     *
     * @param point 3D world position.
     * @param yawRad Yaw rotation in radians (horizontal drag).
     * @param pitchRad Pitch rotation in radians (vertical drag, clamped +/-80 deg).
     * @param centerX Screen center X coordinate.
     * @param centerY Screen center Y coordinate.
     * @param zoom Camera zoom factor (0.5x .. 3.0x).
     * @param focal Distance to camera focal plane (controls perspective distortion).
     */
    fun project(
        point: Point3D,
        yawRad: Float,
        pitchRad: Float,
        centerX: Float,
        centerY: Float,
        zoom: Float = 1.0f,
        focal: Float = 1200f
    ): ProjectedPoint {
        val cosYaw = cos(yawRad.toDouble()).toFloat()
        val sinYaw = sin(yawRad.toDouble()).toFloat()
        val cosPitch = cos(pitchRad.toDouble()).toFloat()
        val sinPitch = sin(pitchRad.toDouble()).toFloat()

        // 1. Yaw rotation around Y axis
        val x1 = point.x * cosYaw + point.z * sinYaw
        val y1 = point.y
        val z1 = -point.x * sinYaw + point.z * cosYaw

        // 2. Pitch rotation around X axis
        val x2 = x1
        val y2 = y1 * cosPitch - z1 * sinPitch
        val z2 = y1 * sinPitch + z1 * cosPitch

        // 3. Perspective projection: scale = focal / (focal + z)
        // Guard against division by zero if point moves behind camera
        val depth = z2
        val denominator = (focal + depth).coerceAtLeast(100f)
        val perspective = focal / denominator
        val effectiveScale = perspective * zoom

        val screenX = centerX + x2 * effectiveScale
        val screenY = centerY + y2 * effectiveScale

        return ProjectedPoint(
            screenX = screenX,
            screenY = screenY,
            depth = depth,
            scale = effectiveScale
        )
    }
}
