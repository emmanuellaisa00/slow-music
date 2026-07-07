package com.slowmusic.app.presentation.screens.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.theme.apple.*
import com.slowmusic.app.presentation.components.apple.*

/**
 * Apple Music-style Full Player Screen
 * Features: Dynamic colors, liquid glass, 120Hz animations, large album art
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleMusicPlayerScreen(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    repeatMode: RepeatMode,
    isShuffled: Boolean,
    isFavorite: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLyrics: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation states
    val albumRotation by animateFloatAsState(
        targetValue = if (isPlaying) 360f * 100 else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "album_rotation"
    )
    
    var showControls by remember { mutableStateOf(true) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppleColors.primary.copy(alpha = 0.6f),
                        AppleColors.background
                    )
                )
            )
    ) {
        // Animated Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Subtle blur for depth
                }
        ) {
            AsyncImage(
                model = song.albumArtUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp)
                    .graphicsLayer { alpha = 0.5f },
                contentScale = ContentScale.Crop
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Minimize",
                            tint = AppleColors.textPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Text(
                        text = "Now Playing",
                        style = AppleTypography.footnote,
                        color = AppleColors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    IconButton(onClick = onNavigateToQueue) {
                        Icon(
                            imageVector = Icons.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = AppleColors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            // Album Art with Rotation Animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .aspectRatio(1f)
                    .clickable { showControls = !showControls },
                contentAlignment = Alignment.Center
            ) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            shadowElevation = 32.dp.toPx()
                        }
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppleColors.primary.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                // Album Art
                AsyncImage(
                    model = song.albumArtUrl,
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(CircleShape)
                        .graphicsLayer {
                            // Subtle floating animation
                            scaleX = 1f
                            scaleY = 1f
                        }
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Song Info
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = AppleTypography.title2,
                                color = AppleColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = song.artist,
                                style = AppleTypography.body,
                                color = AppleColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            if (song.album.isNotEmpty()) {
                                Text(
                                    text = song.album,
                                    style = AppleTypography.footnote,
                                    color = AppleColors.textTertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) AppleColors.secondary else AppleColors.textSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                AppleSlider(
                    value = progress,
                    onValueChange = onSeek,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration((progress * song.duration).toLong()),
                        style = AppleTypography.caption1,
                        color = AppleColors.textTertiary
                    )
                    Text(
                        text = formatDuration(song.duration),
                        style = AppleTypography.caption1,
                        color = AppleColors.textTertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main Controls
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffled) AppleColors.primary else AppleColors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Previous
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous",
                            tint = AppleColors.textPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Play/Pause
                    val playButtonScale by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = springBouncy,
                        label = "play_button"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(playButtonScale)
                            .clip(CircleShape)
                            .background(AppleColors.textPrimary)
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = AppleColors.background,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Next
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint = AppleColors.textPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Repeat
                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.ONE -> Icons.Filled.RepeatOne
                                else -> Icons.Filled.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode != RepeatMode.OFF) AppleColors.primary else AppleColors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Actions
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(initialY = { it }),
                exit = fadeOut() + slideOutVertically(targetY = { it })
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lyrics
                    IconButton(onClick = onNavigateToLyrics) {
                        Icon(
                            imageVector = Icons.Filled.Lyrics,
                            contentDescription = "Lyrics",
                            tint = AppleColors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // AirPlay / Cast
                    IconButton(onClick = { /* Cast */ }) {
                        Icon(
                            imageVector = Icons.Filled.Cast,
                            contentDescription = "Cast",
                            tint = AppleColors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Share
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = AppleColors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // More
                    IconButton(onClick = { /* More options */ }) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "More",
                            tint = AppleColors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = millis / 1000 / 60
    val seconds = (millis / 1000) % 60
    return "%d:%02d".format(minutes, seconds)
}
