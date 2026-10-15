// Location: app/src/main/java/com/vk/clockdin/MainActivity.kt
package com.vk.clockdin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.vk.clockdin.ui.navigation.ClockdinNavHost
import com.vk.clockdin.ui.theme.ClockdinTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ SplashScreen initialization
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Enforce Android 14+ edge-to-edge system bars
        enableEdgeToEdge()

        setContent {
            ClockdinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    val navController = rememberNavController()
                    ClockdinNavHost(navController = navController)
                }
            }
        }
    }
}
