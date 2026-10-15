// Location: app/src/test/java/com/vk/clockdin/domain/usecase/CalculateStreakUseCaseTest.kt
package com.vk.clockdin.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CalculateStreakUseCaseTest {

    private lateinit var useCase: CalculateStreakUseCase
    private val today = LocalDate.of(2026, 10, 15)

    @Before
    fun setup() {
        useCase = CalculateStreakUseCase()
    }

    @Test
    fun emptyListReturnsZeroStreak() {
        val streak = useCase(emptyList(), today)
        assertEquals(0, streak)
    }

    @Test
    fun entryTodayOnlyReturnsStreakOfOne() {
        val dates = listOf("2026-10-15")
        val streak = useCase(dates, today)
        assertEquals(1, streak)
    }

    @Test
    fun entryYesterdayOnlyReturnsStreakOfOne() {
        // Today has no entry yet, but yesterday did -> streak is 1
        val dates = listOf("2026-10-14")
        val streak = useCase(dates, today)
        assertEquals(1, streak)
    }

    @Test
    fun consecutiveDaysEndingTodayCalculatesCorrectStreak() {
        // Entries on 15th, 14th, 13th, 12th -> 4 consecutive days
        val dates = listOf("2026-10-15", "2026-10-14", "2026-10-13", "2026-10-12")
        val streak = useCase(dates, today)
        assertEquals(4, streak)
    }

    @Test
    fun consecutiveDaysEndingYesterdayCalculatesCorrectStreak() {
        // Entries on 14th, 13th, 12th (today 15th has no entry yet) -> 3 days
        val dates = listOf("2026-10-14", "2026-10-13", "2026-10-12")
        val streak = useCase(dates, today)
        assertEquals(3, streak)
    }

    @Test
    fun streakBreaksWhenYesterdayIsMissing() {
        // Entries on today (15th) and 2 days ago (13th), but 14th missing -> streak is 1
        val dates = listOf("2026-10-15", "2026-10-13", "2026-10-12")
        val streak = useCase(dates, today)
        assertEquals(1, streak)
    }

    @Test
    fun streakIsZeroWhenBothTodayAndYesterdayAreMissing() {
        // Entries on 13th and 12th, but today (15th) and yesterday (14th) missing -> streak 0
        val dates = listOf("2026-10-13", "2026-10-12")
        val streak = useCase(dates, today)
        assertEquals(0, streak)
    }

    @Test
    fun duplicateEntriesOnSameDayCountAsSingleDay() {
        // Multiple entries on the same day do not artificially inflate streak
        val dates = listOf("2026-10-15", "2026-10-15", "2026-10-14", "2026-10-14")
        val streak = useCase(dates, today)
        assertEquals(2, streak)
    }

    @Test
    fun unsortedDatesAreHandledCorrectly() {
        val dates = listOf("2026-10-13", "2026-10-15", "2026-10-14")
        val streak = useCase(dates, today)
        assertEquals(3, streak)
    }
}
