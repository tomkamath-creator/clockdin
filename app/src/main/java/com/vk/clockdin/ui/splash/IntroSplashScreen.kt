// Location: app/src/main/java/com/vk/clockdin/ui/splash/IntroSplashScreen.kt
package com.vk.clockdin.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vk.clockdin.R
import com.vk.clockdin.ui.theme.WordmarkStyle
import com.vk.clockdin.ui.theme.WordmarkSubtitleStyle
import com.vk.clockdin.ui.theme.spaceBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Animated intro splash screen:
 * "clockdin" in large, thin, frosted-white type with "by vk" below in smaller, lower-alpha text.
 * Fade + slight spring scale-in, holds ~1s, then navigates to Galaxy Home.
 */
@Composable
fun IntroSplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        // Animate alpha and scale concurrently using spring physics
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Hold for ~1s as specified
        delay(1100L)
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .spaceBackground(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = WordmarkStyle
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.wordmark_subtitle),
                style = WordmarkSubtitleStyle.copy(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            com.vk.clockdin.ui.theme.MoodJoy,
                            com.vk.clockdin.ui.theme.MoodLoving,
                            com.vk.clockdin.ui.theme.MoodAnxious,
                            com.vk.clockdin.ui.theme.MoodCalm
                        )
                    )
                )
            )
        }
    }
}
