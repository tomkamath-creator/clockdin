// Location: app/src/test/java/com/vk/clockdin/domain/math/FibonacciSphereTest.kt
package com.vk.clockdin.domain.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class FibonacciSphereTest {

    @Test
    fun singleEntryPlacesAtOrigin() {
        val point = FibonacciSphere.calculatePosition(index = 0, totalCount = 1)
        assertEquals(0f, point.x, 0.001f)
        assertEquals(0f, point.y, 0.001f)
        assertEquals(0f, point.z, 0.001f)
    }

    @Test
    fun calculatedPositionsAreFiniteAndValid() {
        val totalCount = 50
        for (i in 0 until totalCount) {
            val point = FibonacciSphere.calculatePosition(index = i, totalCount = totalCount)
            assertFalse("x is NaN at index $i", point.x.isNaN())
            assertFalse("y is NaN at index $i", point.y.isNaN())
            assertFalse("z is NaN at index $i", point.z.isNaN())
            assertFalse("x is Infinite at index $i", point.x.isInfinite())
            assertFalse("y is Infinite at index $i", point.y.isInfinite())
            assertFalse("z is Infinite at index $i", point.z.isInfinite())
        }
    }

    @Test
    fun placementIsStrictlyDeterministic() {
        val totalCount = 20
        for (i in 0 until totalCount) {
            val point1 = FibonacciSphere.calculatePosition(index = i, totalCount = totalCount)
            val point2 = FibonacciSphere.calculatePosition(index = i, totalCount = totalCount)
            assertEquals("x must match deterministically", point1.x, point2.x, 0.0001f)
            assertEquals("y must match deterministically", point1.y, point2.y, 0.0001f)
            assertEquals("z must match deterministically", point1.z, point2.z, 0.0001f)
        }
    }

    @Test
    fun radialExpansionFromCenterOutward() {
        val totalCount = 100
        val minRadius = 50f
        val maxRadius = 500f

        val oldest = FibonacciSphere.calculatePosition(
            index = 0,
            totalCount = totalCount,
            maxRadius = maxRadius,
            minRadius = minRadius
        )
        val newest = FibonacciSphere.calculatePosition(
            index = totalCount - 1,
            totalCount = totalCount,
            maxRadius = maxRadius,
            minRadius = minRadius
        )

        val distOldest = sqrt(oldest.x * oldest.x + oldest.y * oldest.y + oldest.z * oldest.z)
        val distNewest = sqrt(newest.x * newest.x + newest.y * newest.y + newest.z * newest.z)

        assertEquals("Oldest entry should be at minRadius", minRadius, distOldest, 1.0f)
        assertEquals("Newest entry should be at maxRadius", maxRadius, distNewest, 1.0f)
        assertTrue("Newest entry must be further from origin than oldest", distNewest > distOldest)
    }

    @Test
    fun projectionProducesValidScreenCoordinates() {
        val point = Point3D(100f, 50f, 20f)
        val projected = FibonacciSphere.project(
            point = point,
            yawRad = 0.5f,
            pitchRad = -0.2f,
            centerX = 500f,
            centerY = 800f,
            zoom = 1.2f,
            focal = 1000f
        )

        assertFalse(projected.screenX.isNaN())
        assertFalse(projected.screenY.isNaN())
        assertFalse(projected.scale.isNaN())
        assertTrue("Scale must be positive", projected.scale > 0f)
    }
}
