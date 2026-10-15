// Location: app/src/test/java/com/vk/clockdin/StreakTest.kt
package com.vk.clockdin

import com.vk.clockdin.domain.usecase.CalculateStreakUseCase
import com.vk.clockdin.domain.usecase.ComputeStreakUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class StreakTest {

    private lateinit var computeStreak: ComputeStreakUseCase
    private val today = LocalDate.of(2026, 10, 15)

    @Before
    fun setup() {
        computeStreak = ComputeStreakUseCase(CalculateStreakUseCase())
    }

    @Test
    fun `empty list returns 0 streak`() {
        assertEquals(0, computeStreak(emptyList(), today))
    }

    @Test
    fun `entry today gives streak 1`() {
        assertEquals(1, computeStreak(listOf("2026-10-15"), today))
    }

    @Test
    fun `entry yesterday only gives streak 1`() {
        assertEquals(1, computeStreak(listOf("2026-10-14"), today))
    }

    @Test
    fun `3 consecutive days ending today gives streak 3`() {
        val dates = listOf("2026-10-15", "2026-10-14", "2026-10-13")
        assertEquals(3, computeStreak(dates, today))
    }

    @Test
    fun `3 consecutive days ending yesterday gives streak 3`() {
        val dates = listOf("2026-10-14", "2026-10-13", "2026-10-12")
        assertEquals(3, computeStreak(dates, today))
    }

    @Test
    fun `gap breaks streak`() {
        val dates = listOf("2026-10-15", "2026-10-13")
        assertEquals(1, computeStreak(dates, today))
    }

    @Test
    fun `duplicate entries on same day do not inflate streak`() {
        val dates = listOf("2026-10-15", "2026-10-15", "2026-10-14")
        assertEquals(2, computeStreak(dates, today))
    }
}
