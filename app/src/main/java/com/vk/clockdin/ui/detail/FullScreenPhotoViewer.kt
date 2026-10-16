// Location: app/src/main/java/com/vk/clockdin/ui/detail/FullScreenPhotoViewer.kt
package com.vk.clockdin.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.PureBlack
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.glassCard
import java.io.File

/**
 * Immersive full-screen photo viewer with horizontal swiping across multiple photos,
 * pinch-to-zoom (up to 4x), double-tap zoom toggle, and predictive back support.
 */
@Composable
fun FullScreenPhotoViewer(
    photoPaths: List<String>,
    initialIndex: Int = 0,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) {
        onClose()
    }

    val validPaths = remember(photoPaths) {
        photoPaths.filter { it.isNotBlank() }
    }

    if (validPaths.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(PureBlack),
            contentAlignment = Alignment.Center
        ) {
            Text("No photo available", color = TextPrimary)
        }
        return
    }

    val clampedInitial = initialIndex.coerceIn(0, validPaths.size - 1)
    val pagerState = rememberPagerState(
        initialPage = clampedInitial,
        pageCount = { validPaths.size }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Horizontal Swiping Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            ZoomablePhotoItem(
                photoPath = validPaths[page],
                modifier = Modifier.fillMaxSize()
            )
        }

        // Close Button in Top-Left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .size(42.dp)
                .glassCard(shape = CircleShape, fillColor = GlassSurfaceLow)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close photo",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Photo Index Counter Pill (Bottom Center)
        if (validPaths.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
                    .glassCard(shape = RoundedCornerShape(16.dp), fillColor = Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${validPaths.size}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Backward compatibility overload for single photo path.
 */
@Composable
fun FullScreenPhotoViewer(
    photoPath: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    FullScreenPhotoViewer(
        photoPaths = listOf(photoPath),
        initialIndex = 0,
        onClose = onClose,
        modifier = modifier
    )
}

@Composable
private fun ZoomablePhotoItem(
    photoPath: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                    offset = if (scale > 1f) {
                        offset + pan
                    } else {
                        Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = File(photoPath),
            contentDescription = "Full-Screen Photo Memory",
            loading = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TextPrimary, strokeWidth = 2.dp)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}
