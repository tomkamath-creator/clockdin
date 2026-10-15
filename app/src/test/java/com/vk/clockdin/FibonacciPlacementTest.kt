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
    fun `single entry places at origin`() {
        val point = FibonacciSphere.calculatePosition(index = 0, totalCount = 1)
        assertEquals(0f, point.x, 0.001f)
        assertEquals(0f, point.y, 0.001f)
        assertEquals(0f, point.z, 0.001f)
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
    fun `points expand from center outwards`() {
        val total = 50
        val oldest = FibonacciSphere.calculatePosition(0, total)
        val newest = FibonacciSphere.calculatePosition(total - 1, total)

        val rOldest = sqrt(oldest.x * oldest.x + oldest.y * oldest.y + oldest.z * oldest.z)
        val rNewest = sqrt(newest.x * newest.x + newest.y * newest.y + newest.z * newest.z)

        assertTrue("Newest entry ($rNewest) > oldest entry ($rOldest)", rNewest > rOldest)
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
