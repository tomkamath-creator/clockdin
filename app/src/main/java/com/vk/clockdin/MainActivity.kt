// app/src/main/java/com/vk/clockdin/MainActivity.kt
package com.vk.clockdin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.vk.clockdin.ui.ClockdinRoot
import com.vk.clockdin.ui.theme.ClockdinTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash before super.onCreate so the system flash is charcoal
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Edge-to-edge: transparent system bars, Compose handles insets
        enableEdgeToEdge()

        setContent {
            ClockdinTheme {
                ClockdinRoot()
            }
        }
    }
}