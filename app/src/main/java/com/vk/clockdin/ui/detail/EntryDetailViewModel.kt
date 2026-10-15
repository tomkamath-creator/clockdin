// Location: app/src/main/java/com/vk/clockdin/ui/detail/EntryDetailViewModel.kt
package com.vk.clockdin.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import com.vk.clockdin.domain.usecase.DeleteEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EntryDetailUiState {
    data object Loading : EntryDetailUiState
    data class Success(val entry: MoodEntry) : EntryDetailUiState
    data object NotFound : EntryDetailUiState
}

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val moodEntryRepository: MoodEntryRepository,
    private val deleteEntryUseCase: DeleteEntryUseCase
) : ViewModel() {

    private val entryId: Long = savedStateHandle.get<Long>("entryId") ?: 0L

    private val _uiState = MutableStateFlow<EntryDetailUiState>(EntryDetailUiState.Loading)
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    init {
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            moodEntryRepository.getEntryById(entryId).collect { entry ->
                _uiState.value = if (entry != null) {
                    EntryDetailUiState.Success(entry)
                } else {
                    EntryDetailUiState.NotFound
                }
            }
        }
    }

    fun deleteEntry(onDeleted: () -> Unit) {
        val currentState = _uiState.value
        if (currentState is EntryDetailUiState.Success) {
            viewModelScope.launch {
                deleteEntryUseCase(currentState.entry)
                onDeleted()
            }
        }
    }
}
