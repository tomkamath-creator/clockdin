// Location: app/src/main/java/com/vk/clockdin/ui/settings/SettingsViewModel.kt
package com.vk.clockdin.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.clockdin.data.backup.BackupManager
import com.vk.clockdin.domain.repository.MoodEntryRepository
import com.vk.clockdin.domain.repository.SettingsRepository
import com.vk.clockdin.work.DailyReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val hapticsEnabled: Boolean = true,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0,
    val lastBackupTimestamp: Long = 0L,
    val totalEntries: Int = 0,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val feedbackMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val moodEntryRepository: MoodEntryRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Collect user settings
        viewModelScope.launch {
            settingsRepository.settingsFlow.collectLatest { settings ->
                _uiState.update {
                    it.copy(
                        hapticsEnabled = settings.hapticsEnabled,
                        reminderEnabled = settings.reminderEnabled,
                        reminderHour = settings.reminderHour,
                        reminderMinute = settings.reminderMinute,
                        lastBackupTimestamp = settings.lastBackupTimestamp
                    )
                }
            }
        }

        // Collect total entry count
        viewModelScope.launch {
            moodEntryRepository.getEntryCount().collectLatest { count ->
                _uiState.update { it.copy(totalEntries = count) }
            }
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticsEnabled(enabled)
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReminderEnabled(enabled)
            val state = _uiState.value
            if (enabled) {
                DailyReminderWorker.schedule(context, state.reminderHour, state.reminderMinute)
            } else {
                DailyReminderWorker.cancel(context)
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setReminderTime(hour, minute)
            if (_uiState.value.reminderEnabled) {
                DailyReminderWorker.schedule(context, hour, minute)
            }
        }
    }

    fun exportBackup(destinationUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val count = backupManager.exportBackup(context, destinationUri)
                val now = System.currentTimeMillis()
                settingsRepository.setLastBackupTimestamp(now)
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        feedbackMessage = "Successfully exported $count mood orbs to archive ✦"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        feedbackMessage = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun importBackup(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                val count = backupManager.importBackup(context, sourceUri)
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        feedbackMessage = "Successfully restored $count mood orbs into your galaxy ✦"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        feedbackMessage = "Restore failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                backupManager.clearAllData(context)
                _uiState.update {
                    it.copy(feedbackMessage = "Galaxy erased. A fresh cosmic canvas awaits.")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(feedbackMessage = "Failed to clear data: ${e.message}")
                }
            }
        }
    }

    fun dismissFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
