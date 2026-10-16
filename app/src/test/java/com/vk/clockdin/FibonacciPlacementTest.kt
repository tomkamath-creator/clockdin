// Location: app/src/test/java/com/vk/clockdin/FibonacciPlacementTest.kt
package com.vk.clockdin

import com.vk.clockdin.domain.math.FibonacciSphere
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

/**
 * Tests deterministic Fibonacci sphere placement used by the galaxy engine.
 */
class FibonacciPlacementTest {

    @Test
    fun `single entry places on outer curved sphere not at origin`() {
        val radius = 360f
        val point = FibonacciSphere.calculatePosition(index = 0, totalCount = 1, sphereRadius = radius)
        val dist = sqrt(point.x * point.x + point.y * point.y + point.z * point.z)
        assertEquals(radius, dist, 0.5f)
        assertTrue("Single first orb must not be frozen at origin", point.x != 0f || point.y != 0f || point.z != 0f)
    }

    @Test
    fun `points are strictly deterministic`() {
        val a = FibonacciSphere.calculatePosition(7, 100)
        val b = FibonacciSphere.calculatePosition(7, 100)
        assertEquals(a.x, b.x, 0.0001f)
        assertEquals(a.y, b.y, 0.0001f)
        assertEquals(a.z, b.z, 0.0001f)
    }

    @Test
    fun `all points float along outer curved sphere`() {
        val total = 50
        val radius = 360f
        for (i in 0 until total) {
            val point = FibonacciSphere.calculatePosition(i, total, sphereRadius = radius)
            val dist = sqrt(point.x * point.x + point.y * point.y + point.z * point.z)
            assertEquals("Point $i must float on outer sphere radius", radius, dist, 1.0f)
        }
    }

    @Test
    fun `coordinates are valid non-NaN finite values`() {
        val total = 100
        repeat(total) { i ->
            val p = FibonacciSphere.calculatePosition(i, total)
            assertFalse(p.x.isNaN())
            assertFalse(p.y.isNaN())
            assertFalse(p.z.isNaN())
            assertFalse(p.x.isInfinite())
            assertFalse(p.y.isInfinite())
            assertFalse(p.z.isInfinite())
        }
    }
}
