// app/src/main/java/com/vk/clockdin/ui/home/GalaxyHomeScreen.kt
package com.vk.clockdin.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Galaxy Home screen shell. The 3D particle engine Canvas is implemented in Phase 2.
 * This scaffold wires the ViewModel, exposes the correct callbacks, and handles
 * one-off events (navigation, toasts).
 */
@Composable
fun GalaxyHomeScreen(
    onNavigateToPaint: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: GalaxyHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // One-off events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GalaxyHomeEvent.NavigateToDetail -> onNavigateToDetail(event.entryId)
                is GalaxyHomeEvent.ShowToast -> { /* handled by snackbar in Phase 2 */ }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF090A0F), Color(0xFF000000))
                )
            )
    ) {
        // Phase 2: GalaxyCanvas() goes here, filling the Box

        // Minimal streak label (header overlay)
        if (!uiState.isLoading) {
            Text(
                text = if (uiState.streak > 0) "\uD83D\uDD25 ${uiState.streak} days" else "Start your streak",
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .safeDrawingPadding()
                    .padding(top = 16.dp)
            )
        }
    }
}