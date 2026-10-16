// Location: app/src/test/java/com/vk/clockdin/domain/math/FibonacciSphereTest.kt
package com.vk.clockdin.domain.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class FibonacciSphereTest {

    @Test
    fun singleEntryPlacesOnCurvedOuterSphereNotAtOrigin() {
        val sphereRadius = 360f
        val point = FibonacciSphere.calculatePosition(index = 0, totalCount = 1, sphereRadius = sphereRadius)
        val dist = sqrt(point.x * point.x + point.y * point.y + point.z * point.z)
        assertEquals(sphereRadius, dist, 0.5f)
        assertTrue("Single orb must not be frozen at (0,0,0)", point.x != 0f || point.y != 0f || point.z != 0f)
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
    fun allEntriesFloatOnOuterSphereRadius() {
        val totalCount = 100
        val sphereRadius = 380f

        for (i in 0 until totalCount) {
            val point = FibonacciSphere.calculatePosition(
                index = i,
                totalCount = totalCount,
                sphereRadius = sphereRadius
            )
            val dist = sqrt(point.x * point.x + point.y * point.y + point.z * point.z)
            assertEquals("Orb $i should be placed on outer sphere radius", sphereRadius, dist, 1.0f)
        }
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
