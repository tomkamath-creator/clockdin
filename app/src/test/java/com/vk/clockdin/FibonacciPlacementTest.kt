// app/src/test/java/com/vk/clockdin/FibonacciPlacementTest.kt
package com.vk.clockdin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Tests the deterministic Fibonacci sphere placement used by the galaxy engine.
 * The full engine lives in Phase 2; this test exercises the math in isolation.
 */
class FibonacciPlacementTest {

    /**
     * Computes the Fibonacci sphere position for bead [index] out of [total].
     * Returns (x, y, z) on a unit sphere.
     *
     * Algorithm:
     *   phi = golden angle = PI * (3 - sqrt(5))
     *   y   = 1 - (2 * index + 1) / total         // maps [-1, 1]
     *   r   = sqrt(1 - y*y)
     *   theta = phi * index
     *   x = r * cos(theta), z = r * sin(theta)
     */
    private fun fibonacciSphere(index: Int, total: Int): Triple<Double, Double, Double> {
        require(total > 0)
        val phi = Math.PI * (3.0 - sqrt(5.0))
        val y = 1.0 - (2.0 * index + 1.0) / total
        val r = sqrt(maxOf(0.0, 1.0 - y * y))
        val theta = phi * index
        return Triple(r * cos(theta), y, r * sin(theta))
    }

    @Test
    fun `points lie on unit sphere`() {
        val total = 50
        repeat(total) { i ->
            val (x, y, z) = fibonacciSphere(i, total)
            val radius = sqrt(x * x + y * y + z * z)
            assertEquals("Point $i should lie on unit sphere", 1.0, radius, 1e-9)
        }
    }

    @Test
    fun `positions are deterministic`() {
        val a = fibonacciSphere(7, 100)
        val b = fibonacciSphere(7, 100)
        assertEquals(a, b)
    }

    @Test
    fun `first point is at top, last near bottom`() {
        val total = 10
        val (_, yFirst, _) = fibonacciSphere(0, total)
        val (_, yLast, _) = fibonacciSphere(total - 1, total)
        // First point y should be > last point y (top to bottom distribution)
        assertTrue("First y ($yFirst) > last y ($yLast)", yFirst > yLast)
    }

    @Test
    fun `points spread over both hemispheres for even total`() {
        val total = 100
        val ys = (0 until total).map { fibonacciSphere(it, total).second }
        val positiveCount = ys.count { it > 0 }
        val negativeCount = ys.count { it < 0 }
        // Roughly equal split
        assertTrue(positiveCount in 40..60)
        assertTrue(negativeCount in 40..60)
    }
}