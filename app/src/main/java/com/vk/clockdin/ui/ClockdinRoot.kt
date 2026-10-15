// app/src/main/java/com/vk/clockdin/ui/ClockdinRoot.kt
package com.vk.clockdin.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.vk.clockdin.ui.navigation.ClockdinNavGraph

/**
 * Root composable wired into [MainActivity]. Hosts the nav graph.
 */
@Composable
fun ClockdinRoot() {
    val navController = rememberNavController()
    ClockdinNavGraph(navController = navController)
}