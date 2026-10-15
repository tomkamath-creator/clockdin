// Location: app/src/main/java/com/vk/clockdin/ui/paint/PaintViewModel.kt
package com.vk.clockdin.ui.paint

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.clockdin.domain.model.MoodColor
import com.vk.clockdin.domain.repository.MoodColorRepository
import com.vk.clockdin.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaintViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moodColorRepository: MoodColorRepository,
    private val settingsRepository: SettingsRepository,
    val saveEntryUseCase: com.vk.clockdin.domain.usecase.SaveEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaintUiState())
    val uiState: StateFlow<PaintUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<PaintEvent>(Channel.BUFFERED)
    val events: Flow<PaintEvent> = _eventChannel.receiveAsFlow()

    private var hapticsEnabled: Boolean = true

    init {
        // Collect user settings
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                hapticsEnabled = settings.hapticsEnabled
            }
        }

        // Collect available mood palette colors
        viewModelScope.launch {
            moodColorRepository.getAllColors().collect { colors ->
                _uiState.update { state ->
                    val defaultSelected = if (state.selectedColors.isEmpty() && colors.size >= 2) {
                        listOf(colors[0], colors[1])
                    } else {
                        state.selectedColors
                    }
                    val active = state.activeColor ?: defaultSelected.firstOrNull()

                    state.copy(
                        availableColors = colors,
                        selectedColors = defaultSelected,
                        activeColor = active
                    )
                }
            }
        }
    }

    fun setMode(mode: PaintMode) {
        val defaultSize = when (mode) {
            PaintMode.BRUSH -> 36f
            PaintMode.SPRAY -> 100f
            PaintMode.WATERCOLOR -> 70f
        }
        _uiState.update { it.copy(mode = mode, brushSize = defaultSize) }
        triggerHaptic()
    }

    fun setActiveColor(color: MoodColor) {
        _uiState.update { state ->
            val updatedSelected = if (!state.selectedColors.contains(color)) {
                if (state.selectedColors.size < 5) {
                    state.selectedColors + color
                } else {
                    state.selectedColors
                }
            } else {
                state.selectedColors
            }
            state.copy(
                activeColor = color,
                selectedColors = updatedSelected
            )
        }
        triggerHaptic()
    }

    fun toggleMoodColor(color: MoodColor) {
        val current = _uiState.value.selectedColors
        if (current.contains(color)) {
            if (current.size <= 2) {
                // Minimum 2 colors required
                triggerShake("An orb requires at least 2 mood colors")
                return
            }
            val updated = current - color
            _uiState.update {
                it.copy(
                    selectedColors = updated,
                    activeColor = if (it.activeColor == color) updated.firstOrNull() else it.activeColor,
                    hasUnsavedChanges = true
                )
            }
        } else {
            if (current.size >= 5) {
                // Maximum 5 colors allowed
                triggerShake("Select a maximum of 5 mood colors")
                return
            }
            val updated = current + color
            _uiState.update {
                it.copy(
                    selectedColors = updated,
                    activeColor = color,
                    hasUnsavedChanges = true
                )
            }
        }
        triggerHaptic()
    }

    fun addStroke(stroke: PaintStroke) {
        _uiState.update {
            it.copy(
                strokes = it.strokes + stroke,
                hasUnsavedChanges = true
            )
        }
    }

    fun undoStroke() {
        val current = _uiState.value.strokes
        if (current.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    strokes = current.dropLast(1),
                    hasUnsavedChanges = (current.size - 1) > 0
                )
            }
            triggerHaptic()
        }
    }

    fun clearCanvas() {
        _uiState.update {
            it.copy(
                strokes = emptyList(),
                hasUnsavedChanges = false
            )
        }
        triggerHaptic()
    }

    fun setBrushSize(size: Float) {
        _uiState.update { it.copy(brushSize = size.coerceIn(12f, 200f)) }
    }

    fun onNextClicked(canvasSizePx: Float) {
        val state = _uiState.value
        if (state.selectedColors.size < 2) {
            triggerShake("Select at least 2 mood colors")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRasterizing = true) }

            val imagePath = OrbRasterizer.rasterizeToFile(
                context = context,
                strokes = state.strokes,
                canvasSize = canvasSizePx
            )

            val dominantStops = OrbRasterizer.extractDominantStops(
                strokes = state.strokes,
                fallbackColors = state.selectedColors
            )

            _uiState.update { it.copy(isRasterizing = false) }
            _eventChannel.send(PaintEvent.ProceedToSave(imagePath, dominantStops))
        }
    }

    private fun triggerShake(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isShaking = true, validationMessage = message) }
            triggerHaptic()
            kotlinx.coroutines.delay(400L)
            _uiState.update { it.copy(isShaking = false, validationMessage = null) }
        }
    }

    private fun triggerHaptic() {
        if (hapticsEnabled) {
            viewModelScope.launch {
                _eventChannel.send(PaintEvent.TriggerHaptic)
            }
        }
    }
}
