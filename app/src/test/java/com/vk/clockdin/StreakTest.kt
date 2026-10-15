// app/src/test/java/com/vk/clockdin/StreakTest.kt
package com.vk.clockdin

import com.vk.clockdin.domain.usecase.ComputeStreakUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakTest {

    private val useCase = ComputeStreakUseCase()
    private val today = LocalDate.of(2024, 11, 15)

    @Test
    fun `empty list returns 0`() {
        assertEquals(0, useCase(emptyList(), today))
    }

    @Test
    fun `single entry today returns 1`() {
        assertEquals(1, useCase(listOf("2024-11-15"), today))
    }

    @Test
    fun `consecutive days ending today`() {
        val dates = listOf("2024-11-13", "2024-11-14", "2024-11-15")
        assertEquals(3, useCase(dates, today))
    }

    @Test
    fun `gap breaks streak`() {
        val dates = listOf("2024-11-10", "2024-11-14", "2024-11-15")
        assertEquals(2, useCase(dates, today))
    }

    @Test
    fun `today missing but yesterday present counts from yesterday`() {
        val dates = listOf("2024-11-13", "2024-11-14")
        assertEquals(2, useCase(dates, today))
    }

    @Test
    fun `neither today nor yesterday - returns 0`() {
        val dates = listOf("2024-11-10")
        assertEquals(0, useCase(dates, today))
    }

    @Test
    fun `duplicate dates counted once`() {
        val dates = listOf("2024-11-14", "2024-11-14", "2024-11-15", "2024-11-15")
        assertEquals(2, useCase(dates, today))
    }
}