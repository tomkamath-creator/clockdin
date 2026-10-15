// Location: app/src/main/java/com/vk/clockdin/ui/list/JournalListViewModel.kt
package com.vk.clockdin.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.model.WeeklyMoodStats
import com.vk.clockdin.domain.repository.MoodEntryRepository
import com.vk.clockdin.domain.usecase.CalculateStreakUseCase
import com.vk.clockdin.domain.usecase.CalculateWeeklyMoodStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class JournalListUiState(
    val groupedEntries: Map<String, List<MoodEntry>> = emptyMap(),
    val streakCount: Int = 0,
    val weeklyStats: WeeklyMoodStats = WeeklyMoodStats(),
    val isLoading: Boolean = true
)

@HiltViewModel
class JournalListViewModel @Inject constructor(
    private val moodEntryRepository: MoodEntryRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val calculateWeeklyMoodStatsUseCase: CalculateWeeklyMoodStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalListUiState())
    val uiState: StateFlow<JournalListUiState> = _uiState.asStateFlow()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            moodEntryRepository.getAllEntries().collectLatest { entries ->
                val dates = entries.map { it.localDate }
                val streak = calculateStreakUseCase(dates)
                val weekly = calculateWeeklyMoodStatsUseCase(entries)

                // Group entries by "MMMM yyyy" (e.g. "October 2026")
                val grouped = entries.groupBy { entry ->
                    try {
                        val parsed = LocalDate.parse(entry.localDate)
                        parsed.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
                    } catch (_: Exception) {
                        "Cosmic Archives"
                    }
                }

                _uiState.update {
                    it.copy(
                        groupedEntries = grouped,
                        streakCount = streak,
                        weeklyStats = weekly,
                        isLoading = false
                    )
                }
            }
        }
    }
}
