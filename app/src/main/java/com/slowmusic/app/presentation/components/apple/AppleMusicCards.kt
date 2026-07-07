@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.slowmusic.app.presentation.components.apple

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.theme.apple.*


private enum class SwipeToDismissBoxValue { Settled, StartToEnd, EndToStart }
private class SimpleSwipeToDismissBoxState
@Composable
private fun rememberSwipeToDismissBoxState(confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = { true }): SimpleSwipeToDismissBoxState = SimpleSwipeToDismissBoxState()
@Composable
private fun SwipeToDismissBox(
    state: SimpleSwipeToDismissBoxState,
    modifier: Modifier = Modifier,
    backgroundContent: @Composable () -> Unit = {},
    endBackgroundContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) { Box(modifier) { content() } }

/**
 * Apple Music Style Song Card (Horizontal Card)
 */
@Composable
fun AppleSongCard(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    isPlaying: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                onClick = onClick,
                onClickLabel = "Play"
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onMoreClick,
                onClickLabel = "Play",
                onLongClickLabel = "More options"
            ),
        color = AppleColors.glassWhite.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art with playing indicator
            Box(
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.albumArtUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = if (isPlaying) 2.dp else 0.dp,
                            color = if (isPlaying) AppleColors.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
                
                // Playing indicator
                androidx.compose.animation.AnimatedVisibility(
                    visible = isPlaying,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayingIndicator()
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Song info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = AppleTypography.body,
                    color = if (isPlaying) AppleColors.primary else AppleColors.textPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = AppleTypography.footnote,
                    color = AppleColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Duration
                Text(
                    text = formatDuration(song.duration),
                    style = AppleTypography.caption1,
                    color = AppleColors.textTertiary
                )
            }
            
            // Favorite button
            if (onFavoriteClick != null) {
                IconButton(onClick = onFavoriteClick) {
                    val heartScale by animateFloatAsState(
                        targetValue = if (isFavorite) 1.2f else 1f,
                        animationSpec = AppleSpringAnimations.springBouncy,
                        label = "heart_scale"
                    )
                    
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) AppleColors.secondary else AppleColors.textSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .scale(heartScale)
                    )
                }
            }
            
            // More button
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = "More",
                    tint = AppleColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Apple Music Style Album Card (Grid)
 */
@Composable
fun AppleAlbumCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isHovered -> 1.03f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "album_scale"
    )
    
    Card(
        modifier = modifier
            .width(160.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            Column {
                // Album art
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = album.artworkUrl,
                        contentDescription = album.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Hover overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isHovered,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            AppleColors.primary.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )
                    }
                    
                    // Play button overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppleColors.primary)
                            .graphicsLayer {
                                alpha = if (isHovered) 1f else 0f
                                scaleX = if (isHovered) 1f else 0.5f
                                scaleY = if (isHovered) 1f else 0.5f
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Album info
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = album.title,
                        style = AppleTypography.subheadline,
                        color = AppleColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = album.artist,
                        style = AppleTypography.footnote,
                        color = AppleColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Border glow on hover
            if (isHovered) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    AppleColors.primary.copy(alpha = 0.5f),
                                    AppleColors.primary.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
        }
    }
}

/**
 * Apple Music Style Artist Card
 */
@Composable
fun AppleArtistCard(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artist image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    width = if (isHovered) 3.dp else 0.dp,
                    brush = Brush.linearGradient(AppleColors.gradientPink),
                    shape = CircleShape
                )
        ) {
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = artist.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Hover overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = artist.name,
            style = AppleTypography.subheadline,
            color = AppleColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "Artist",
            style = AppleTypography.caption1,
            color = AppleColors.textSecondary
        )
    }
}

/**
 * Apple Music Style Playlist Card
 */
@Composable
fun ApplePlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppleSongCard(
        song = Song(
            id = playlist.id,
            title = playlist.name,
            artist = "${playlist.songIds.size} songs",
            album = playlist.description ?: "Playlist",
            albumArtUrl = playlist.artworkUrl,
            previewUrl = null,
            streamUrl = null,
            duration = 0,
            genre = null,
            releaseDate = null
        ),
        onClick = onClick,
        onMoreClick = onMoreClick,
        modifier = modifier
    )
}

/**
 * Playing Indicator Animation
 */
@Composable
fun PlayingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "playing")
        
        repeat(3) { index ->
            val heightAnim by infiniteTransition.animateFloat(
                initialValue = 4f,
                targetValue = 16f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 400,
                        delayMillis = index * 100
                    ),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(heightAnim.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppleColors.primary)
            )
        }
    }
}

/**
 * Apple Music Style Swipeable List Item
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleSwipeableListItem(
    song: Song,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onRemove()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onAddToPlaylist()
                    false
                }
                else -> false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            // Background for start swipe (add to playlist)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppleColors.primary)
                    .padding(start = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlaylistAdd,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to Playlist",
                        style = AppleTypography.body,
                        color = Color.White
                    )
                }
            }
        },
        endBackgroundContent = {
            // Background for end swipe (remove)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppleColors.secondary)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remove",
                        style = AppleTypography.body,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        AppleSongCard(
            song = song,
            onClick = onClick,
            onMoreClick = { /* Show bottom sheet */ }
        )
    }
}

/**
 * Apple Music Style Section Header
 */
@Composable
fun AppleSectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppleTypography.title3,
            color = AppleColors.textPrimary,
            fontWeight = FontWeight.Bold
        )
        
        if (onSeeAllClick != null) {
            TextButton(onClick = onSeeAllClick) {
                Text(
                    text = "See All",
                    style = AppleTypography.subheadline,
                    color = AppleColors.primary
                )
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = millis / 1000 / 60
    val seconds = (millis / 1000) % 60
    return "%d:%02d".format(minutes, seconds)
}
