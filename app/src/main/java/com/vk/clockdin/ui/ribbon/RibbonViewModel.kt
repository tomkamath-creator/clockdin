// Location: app/src/main/java/com/vk/clockdin/ui/ribbon/RibbonViewModel.kt
package com.vk.clockdin.ui.ribbon

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
import javax.inject.Inject

data class RibbonUiState(
    val entries: List<MoodEntry> = emptyList(),
    val streakCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class RibbonViewModel @Inject constructor(
    private val moodEntryRepository: MoodEntryRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RibbonUiState())
    val uiState: StateFlow<RibbonUiState> = _uiState.asStateFlow()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            moodEntryRepository.getAllEntriesAscending().collectLatest { entries ->
                val streak = calculateStreakUseCase(entries.map { it.localDate })
                _uiState.update {
                    it.copy(
                        entries = entries,
                        streakCount = streak,
                        isLoading = false
                    )
                }
            }
        }
    }
}
