// app/src/main/java/com/vk/clockdin/ui/navigation/ClockdinNavGraph.kt
package com.vk.clockdin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vk.clockdin.ui.home.GalaxyHomeScreen

/** All type-safe navigation destinations. */
sealed class Screen(val route: String) {
    data object Intro : Screen("intro")
    data object GalaxyHome : Screen("galaxy_home")
    data object Paint : Screen("paint")
    data object Settings : Screen("settings")
    data object PaletteEditor : Screen("palette_editor")
    data class Detail(val entryId: Long = -1L) : Screen("detail/{entryId}") {
        companion object {
            const val ROUTE = "detail/{entryId}"
            const val ARG = "entryId"
            fun buildRoute(id: Long) = "detail/$id"
        }
    }
}

@Composable
fun ClockdinNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Intro.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Intro.route) {
            IntroScreen(onFinished = {
                navController.navigate(Screen.GalaxyHome.route) {
                    popUpTo(Screen.Intro.route) { inclusive = true }
                }
            })
        }

        composable(Screen.GalaxyHome.route) {
            GalaxyHomeScreen(
                onNavigateToPaint = { navController.navigate(Screen.Paint.route) },
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.Detail.buildRoute(id))
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Paint.route) {
            // Implemented in Phase 3
            PlaceholderScreen("Paint Screen — Phase 3")
        }

        composable(
            route = Screen.Detail.ROUTE,
            arguments = listOf(navArgument(Screen.Detail.ARG) { type = NavType.LongType })
        ) {
            // Implemented in Phase 4
            PlaceholderScreen("Detail Screen — Phase 4")
        }

        composable(Screen.Settings.route) {
            // Implemented in Phase 6
            PlaceholderScreen("Settings — Phase 6")
        }

        composable(Screen.PaletteEditor.route) {
            PlaceholderScreen("Palette Editor — Phase 6")
        }
    }
}