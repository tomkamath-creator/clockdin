// Location: app/src/test/java/com/vk/clockdin/domain/usecase/SaveEntryUseCaseTest.kt
package com.vk.clockdin.domain.usecase

import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class SaveEntryUseCaseTest {

    private lateinit var fakeRepository: FakeMoodEntryRepository
    private lateinit var useCase: SaveEntryUseCase

    @Before
    fun setup() {
        fakeRepository = FakeMoodEntryRepository()
        // Provide dummy application context or test via mock
        val dummyContext = android.content.ContextWrapper(null)
        useCase = SaveEntryUseCase(dummyContext, fakeRepository)
    }

    @Test
    fun saveEntryDirect_persistsEntryAndAssignsId() = runBlocking {
        val entry = MoodEntry(
            id = 0L,
            createdAt = 1700000000000L,
            localDate = "2026-10-15",
            gradientStops = listOf(
                GradientStop(hex = "#FFC83D", position = 0f, label = "Joy"),
                GradientStop(hex = "#2FD4A7", position = 1f, label = "Calm")
            ),
            orbImagePath = "/test/orbs/orb_1.png",
            note = "A wonderful cosmic day",
            songTitle = "Starman",
            songArtist = "David Bowie"
        )

        val id = useCase(entry)

        assertEquals(1L, id)
        assertEquals(1, fakeRepository.entries.size)
        val saved = fakeRepository.entries.first()
        assertEquals("2026-10-15", saved.localDate)
        assertEquals("A wonderful cosmic day", saved.note)
        assertEquals("Starman", saved.songTitle)
    }

    private class FakeMoodEntryRepository : MoodEntryRepository {
        val entries = mutableListOf<MoodEntry>()
        private var nextId = 1L

        override fun getAllEntries(): Flow<List<MoodEntry>> = flowOf(entries)
        override fun getAllEntriesAscending(): Flow<List<MoodEntry>> = flowOf(entries)
        override fun getEntryById(id: Long): Flow<MoodEntry?> = flowOf(entries.find { it.id == id })
        override suspend fun getEntryByIdDirect(id: Long): MoodEntry? = entries.find { it.id == id }
        override fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<MoodEntry>> = flowOf(entries)
        override fun getEntriesByMonth(yearMonth: String): Flow<List<MoodEntry>> = flowOf(entries)
        override fun getEntriesForDate(date: String): Flow<List<MoodEntry>> = flowOf(entries.filter { it.localDate == date })
        override fun getAllDistinctDates(): Flow<List<String>> = flowOf(entries.map { it.localDate }.distinct())
        override fun getEntryCount(): Flow<Int> = flowOf(entries.size)
        override suspend fun insertEntry(entry: MoodEntry): Long {
            val assigned = if (entry.id == 0L) entry.copy(id = nextId++) else entry
            entries.add(assigned)
            return assigned.id
        }
        override suspend fun insertEntries(entries: List<MoodEntry>) {
            this.entries.addAll(entries)
        }
        override suspend fun updateEntry(entry: MoodEntry) {
            val idx = entries.indexOfFirst { it.id == entry.id }
            if (idx != -1) entries[idx] = entry
        }
        override suspend fun deleteEntry(entry: MoodEntry) {
            entries.removeAll { it.id == entry.id }
        }
        override suspend fun deleteEntryById(id: Long) {
            entries.removeAll { it.id == id }
        }
        override suspend fun deleteAllEntries() {
            entries.clear()
        }
    }
}
