package com.slowmusic.app.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * inside individual screens. It only activates while audio is actively playing,
 * and it intentionally excludes routes where a stable app background is better
 * UX: home, settings, legal, permissions, cast, splash/onboarding, and the full
 * player which already owns its own artwork background.
 */
fun shouldShowPlaybackArtworkBackdrop(
    currentRoute: String?,
    playbackState: PlaybackState,
    song: Song?
): Boolean {
    return playbackState == PlaybackState.PLAYING &&
        song?.albumArtUrl?.isNotBlank() == true &&
        currentRoute.allowsPlaybackArtworkBackdrop()
}

private fun String?.allowsPlaybackArtworkBackdrop(): Boolean {
    val route = this ?: return false
    if (route == Screen.Home.route || route == Screen.Splash.route || route == Screen.Onboarding.route) return false
    if (route == Screen.Player.route) return false
    if (route.startsWith("settings")) return false
    if (route.startsWith("legal/")) return false
    if (route.startsWith("permissions/")) return false
    if (route.startsWith("cast/")) return false
    return true
}

@Composable
fun PlaybackArtworkBackdrop(
    song: Song?,
    playbackState: PlaybackState,
    currentRoute: String?,
    appleStyle: Boolean,
    modifier: Modifier = Modifier
) {
    val visible = shouldShowPlaybackArtworkBackdrop(currentRoute, playbackState, song)
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow),
        label = "playback_artwork_backdrop_alpha"
    )
    val imageScale by animateFloatAsState(
        targetValue = if (visible) 1.28f else 1.12f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow),
        label = "playback_artwork_backdrop_bloom"
    )
    val blurRadius by animateDpAsState(
        targetValue = if (visible) if (appleStyle) 82.dp else 58.dp else 24.dp,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow),
        label = "playback_artwork_blur_radius"
    )
    val dark = isSystemInDarkTheme() || appleStyle
    val base = if (appleStyle) Color.Transparent else MaterialTheme.colorScheme.background

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
                contentScale = ContentScale.Crop
            )

            // First scrim preserves readability and avoids the raw image feeling
            // like a wallpaper pasted behind unrelated screens.
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
                    .background(
                        Brush.verticalGradient(
                            colors = if (dark) {
                                listOf(
                                    Color.Black.copy(alpha = 0.50f),
                                    Color.Black.copy(alpha = 0.66f),
                                    Color.Black.copy(alpha = 0.82f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.46f),
                                    Color.White.copy(alpha = 0.58f),
                                    Color.White.copy(alpha = 0.72f)
                                )
                            }
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
                                if (dark) Color.Black.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.24f)
                            ),
                            radius = 980f
                        )
                    )
            )
        }
    }
}
