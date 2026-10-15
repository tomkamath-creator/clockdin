// app/src/main/java/com/vk/clockdin/ui/home/GalaxyHomeViewModel.kt
package com.vk.clockdin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import com.vk.clockdin.domain.usecase.ComputeStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class GalaxyHomeUiState(
    val entries: List<MoodEntry> = emptyList(),
    val streak: Int = 0,
    val isLoading: Boolean = true,
    /** The currently active view tab: galaxy / list / calendar / ribbon */
    val activeView: HomeView = HomeView.GALAXY
)

enum class HomeView { GALAXY, LIST, CALENDAR, RIBBON }

sealed class GalaxyHomeEvent {
    data class NavigateToDetail(val entryId: Long) : GalaxyHomeEvent()
    data class ShowToast(val message: String) : GalaxyHomeEvent()
}

@HiltViewModel
class GalaxyHomeViewModel @Inject constructor(
    private val repo: MoodEntryRepository,
    private val computeStreak: ComputeStreakUseCase
) : ViewModel() {

    private val _internalState = MutableStateFlow(GalaxyHomeUiState())

    private val _events = Channel<GalaxyHomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<GalaxyHomeUiState> = combine(
        repo.observeAll(),
        _internalState
    ) { entries, state ->
        val dates = entries.map { it.localDate }
        state.copy(
            entries = entries,
            streak = computeStreak(dates),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GalaxyHomeUiState()
    )

    fun onViewSwitched(view: HomeView) {
        _internalState.update { it.copy(activeView = view) }
    }

    fun onBadTap() {
        // No bead hit — no-op or subtle feedback
    }

    fun onBead(entryId: Long) {
        _events.trySend(GalaxyHomeEvent.NavigateToDetail(entryId))
    }
}