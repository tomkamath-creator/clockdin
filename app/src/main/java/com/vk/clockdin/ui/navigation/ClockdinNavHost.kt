// Location: app/src/main/java/com/vk/clockdin/ui/navigation/ClockdinNavHost.kt
package com.vk.clockdin.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.ui.splash.IntroSplashScreen

@Composable
fun ClockdinNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Flight trajectory payload for newly saved orb landing into 3D galaxy
    var newlySavedOrb by remember { mutableStateOf<Pair<String, List<GradientStop>>?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.96f, animationSpec = tween(240, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                scaleOut(targetScale = 1.02f, animationSpec = tween(180, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 1.02f, animationSpec = tween(240, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                scaleOut(targetScale = 0.96f, animationSpec = tween(180, easing = FastOutSlowInEasing))
        }
    ) {
        composable(
            route = Screen.Splash.route,
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            IntroSplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Galaxy.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Galaxy.route,
            enterTransition = { fadeIn(animationSpec = tween(240)) },
            popEnterTransition = { fadeIn(animationSpec = tween(240)) }
        ) {
            com.vk.clockdin.ui.galaxy.GalaxyHomeScreen(
                newlySavedOrb = newlySavedOrb,
                onSaveAnimationComplete = { newlySavedOrb = null },
                onNavigateToPaint = { navController.navigate(Screen.Paint.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToView = { route -> navController.navigate(route) },
                onOpenDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(
            route = Screen.Paint.route,
            arguments = listOf(
                navArgument("editEntryId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { (it * 0.10f).toInt() },
                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { (it * 0.10f).toInt() },
                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { (it * 0.10f).toInt() },
                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
            }
        ) {
            com.vk.clockdin.ui.paint.PaintScreen(
                onBack = { navController.popBackStack() },
                onProceedToSave = { orbPath, colors ->
                    newlySavedOrb = Pair(orbPath, colors)
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.List.route) {
            com.vk.clockdin.ui.list.JournalListScreen(
                onNavigateToPerspective = { perspective ->
                    if (perspective.route == Screen.Galaxy.route) {
                        navController.navigate(Screen.Galaxy.route) {
                            popUpTo(Screen.Galaxy.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(perspective.route) {
                            popUpTo(Screen.Galaxy.route)
                        }
                    }
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPaint = { navController.navigate(Screen.Paint.route) },
                onEntryClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(route = Screen.Calendar.route) {
            com.vk.clockdin.ui.calendar.CalendarScreen(
                onNavigateToPerspective = { perspective ->
                    if (perspective.route == Screen.Galaxy.route) {
                        navController.navigate(Screen.Galaxy.route) {
                            popUpTo(Screen.Galaxy.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(perspective.route) {
                            popUpTo(Screen.Galaxy.route)
                        }
                    }
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPaint = { navController.navigate(Screen.Paint.route) },
                onEntryClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(route = Screen.Ribbon.route) {
            com.vk.clockdin.ui.ribbon.RibbonScreen(
                onNavigateToPerspective = { perspective ->
                    if (perspective.route == Screen.Galaxy.route) {
                        navController.navigate(Screen.Galaxy.route) {
                            popUpTo(Screen.Galaxy.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(perspective.route) {
                            popUpTo(Screen.Galaxy.route)
                        }
                    }
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPaint = { navController.navigate(Screen.Paint.route) },
                onEntryClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { (it * 0.25f).toInt() },
                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.25f).toInt() },
                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.25f).toInt() },
                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
            }
        ) {
            com.vk.clockdin.ui.settings.SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { (it * 0.08f).toInt() },
                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { (it * 0.08f).toInt() },
                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
            }
        ) {
            com.vk.clockdin.ui.detail.EntryDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenPhoto = { paths, initialIndex ->
                    navController.navigate(Screen.PhotoViewer.createRoute(paths, initialIndex))
                },
                onRepaintOrb = { entryId ->
                    navController.navigate(Screen.Paint.createRoute(entryId))
                }
            )
        }

        composable(
            route = Screen.PhotoViewer.route,
            arguments = listOf(
                navArgument("photoPaths") { type = NavType.StringType },
                navArgument("initialIndex") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.92f, animationSpec = tween(250))
            }
        ) { backStackEntry ->
            val encodedPaths = backStackEntry.arguments?.getString("photoPaths") ?: ""
            val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0
            val decodedPaths = try {
                java.net.URLDecoder.decode(encodedPaths, java.nio.charset.StandardCharsets.UTF_8.name())
            } catch (_: Exception) {
                encodedPaths
            }.split("|||").filter { it.isNotBlank() }

            com.vk.clockdin.ui.detail.FullScreenPhotoViewer(
                photoPaths = decodedPaths,
                initialIndex = initialIndex,
                onClose = { navController.popBackStack() }
            )
        }
    }
}
