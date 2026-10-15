// Location: app/src/main/java/com/vk/clockdin/ui/calendar/CalendarViewModel.kt
package com.vk.clockdin.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import com.vk.clockdin.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val entriesByDate: Map<String, MoodEntry> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),
    val streakCount: Int = 0,
    val isLoading: Boolean = true
) {
    val selectedEntry: MoodEntry?
        get() = entriesByDate[selectedDate.toString()]
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val moodEntryRepository: MoodEntryRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            moodEntryRepository.getAllEntries().collectLatest { entries ->
                val dateMap = entries.associateBy { it.localDate }
                val streak = calculateStreakUseCase(entries.map { it.localDate })

                _uiState.update {
                    it.copy(
                        entriesByDate = dateMap,
                        streakCount = streak,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun previousMonth() {
        _uiState.update {
            val prev = it.currentYearMonth.minusMonths(1)
            it.copy(currentYearMonth = prev)
        }
    }

    fun nextMonth() {
        _uiState.update {
            val next = it.currentYearMonth.plusMonths(1)
            it.copy(currentYearMonth = next)
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }
}
