package com.slowmusic.app.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.slowmusic.app.domain.model.PlaybackState
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.presentation.navigation.Screen

/**
 * Single architecture point for the currently-playing artwork backdrop.
 *
 * This keeps the shell rule explicit instead of scattering album-art blur logic
 * inside individual screens. It follows the active song, uses a dark readability
 * system everywhere, and intentionally excludes only startup/onboarding and the
 * full player which already owns its own artwork background.
 */
fun shouldShowPlaybackArtworkBackdrop(
    currentRoute: String?,
    playbackState: PlaybackState,
    song: Song?,
    enabled: Boolean = true
): Boolean {
    return enabled &&
        playbackState != PlaybackState.IDLE &&
        playbackState != PlaybackState.ERROR &&
        song?.albumArtUrl?.isNotBlank() == true &&
        currentRoute.allowsPlaybackArtworkBackdrop()
}

private fun String?.allowsPlaybackArtworkBackdrop(): Boolean {
    val route = this ?: return false
    if (route == Screen.Splash.route || route == Screen.Onboarding.route) return false
    if (route == Screen.Player.route) return false
    return true
}

@Composable
fun PlaybackArtworkBackdrop(
    song: Song?,
    playbackState: PlaybackState,
    currentRoute: String?,
    appleStyle: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val visible = shouldShowPlaybackArtworkBackdrop(currentRoute, playbackState, song, enabled)
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "playback_artwork_backdrop_alpha"
    )
    val imageScale by animateFloatAsState(
        targetValue = if (visible) 1.32f else 1.20f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow),
        label = "playback_artwork_backdrop_bloom"
    )
    val blurRadius by animateDpAsState(
        targetValue = if (visible) if (appleStyle) 60.dp else 56.dp else 40.dp,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow),
        label = "playback_artwork_blur_radius"
    )
    val base = if (appleStyle) Color.Transparent else MaterialTheme.colorScheme.background
    val desaturateMatrix = remember { ColorMatrix().apply { setToSaturation(0.58f) } }
    val overlayTop = 0.82f
    val overlayMiddle = 0.86f
    val overlayBottom = 0.92f

    Box(modifier.fillMaxSize().background(base)) {
        if (alpha > 0.01f && song?.albumArtUrl?.isNotBlank() == true) {
            AsyncImage(
                model = song.albumArtUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
                    .scale(imageScale)
                    .blur(blurRadius),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(desaturateMatrix)
            )

            // Readability scrim: album art should create atmosphere, never fight content.
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = overlayTop),
                                Color.Black.copy(alpha = overlayMiddle),
                                Color.Black.copy(alpha = overlayBottom)
                            )
                        )
                    )
            )

            // Subtle vertical fade: transparent feeling up top, nearly black at bottom.
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.24f),
                                Color.Black.copy(alpha = 0.72f)
                            )
                        )
                    )
            )

            // Subtle edge vignette gives Spotify-like depth without making the
            // default Android mode look like the Glass skin.
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.56f)
                            ),
                            radius = 920f
                        )
                    )
            )
        }
    }
}
