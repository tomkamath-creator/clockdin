// Location: app/src/main/java/com/vk/clockdin/ui/galaxy/GalaxyHomeViewModel.kt
package com.vk.clockdin.ui.galaxy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.model.WeeklyMoodStats
import com.vk.clockdin.domain.repository.MoodEntryRepository
import com.vk.clockdin.domain.repository.SettingsRepository
import com.vk.clockdin.domain.usecase.CalculateStreakUseCase
import com.vk.clockdin.domain.usecase.CalculateWeeklyMoodStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class GalaxyHomeUiState(
    val entries: List<MoodEntry> = emptyList(),
    val streakCount: Int = 0,
    val hasEntryToday: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val weeklyStats: WeeklyMoodStats = WeeklyMoodStats(),
    val isLoading: Boolean = false
)

sealed interface GalaxyEvent {
    data class OpenDetail(val entryId: Long) : GalaxyEvent
    data object OpenPaint : GalaxyEvent
    data object OpenSettings : GalaxyEvent
    data class SwitchView(val route: String) : GalaxyEvent
    data object PerformHaptic : GalaxyEvent
}

@HiltViewModel
class GalaxyHomeViewModel @Inject constructor(
    private val moodEntryRepository: MoodEntryRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val calculateWeeklyMoodStatsUseCase: CalculateWeeklyMoodStatsUseCase
) : ViewModel() {

    private val _eventChannel = Channel<GalaxyEvent>(Channel.BUFFERED)
    val events: Flow<GalaxyEvent> = _eventChannel.receiveAsFlow()

    val uiState: StateFlow<GalaxyHomeUiState> = combine(
        moodEntryRepository.getAllEntriesAscending(),
        settingsRepository.settingsFlow
    ) { entries, settings ->
        val todayStr = LocalDate.now().toString()
        val allDates = entries.map { it.localDate }
        val streak = calculateStreakUseCase(dates = allDates)
        val hasToday = entries.any { it.localDate == todayStr }
        val weekly = calculateWeeklyMoodStatsUseCase(entries)

        GalaxyHomeUiState(
            entries = entries,
            streakCount = streak,
            hasEntryToday = hasToday,
            hapticsEnabled = settings.hapticsEnabled,
            weeklyStats = weekly,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = GalaxyHomeUiState(isLoading = true)
    )

    fun onBeadTapped(entryId: Long) {
        viewModelScope.launch {
            if (uiState.value.hapticsEnabled) {
                _eventChannel.send(GalaxyEvent.PerformHaptic)
            }
            _eventChannel.send(GalaxyEvent.OpenDetail(entryId))
        }
    }

    fun onFabClicked() {
        viewModelScope.launch {
            if (uiState.value.hapticsEnabled) {
                _eventChannel.send(GalaxyEvent.PerformHaptic)
            }
            _eventChannel.send(GalaxyEvent.OpenPaint)
        }
    }

    fun onSettingsClicked() {
        viewModelScope.launch {
            _eventChannel.send(GalaxyEvent.OpenSettings)
        }
    }

    fun onViewSelected(route: String) {
        viewModelScope.launch {
            _eventChannel.send(GalaxyEvent.SwitchView(route))
        }
    }
}
