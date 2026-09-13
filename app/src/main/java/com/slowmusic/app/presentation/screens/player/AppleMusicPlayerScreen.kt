package com.slowmusic.app.presentation.screens.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.slowmusic.app.domain.model.AudioLayerMode
import com.slowmusic.app.domain.model.RepeatMode
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.presentation.theme.apple.AppleColors
import com.slowmusic.app.presentation.theme.apple.AppleTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppleMusicPlayerScreen(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    repeatMode: RepeatMode,
    audioLayerMode: AudioLayerMode,
    isShuffled: Boolean,
    isFavorite: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSetAudioLayerMode: (AudioLayerMode) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLyrics: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToCast: () -> Unit = {},
    onMoreOptions: () -> Unit = {},
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var swipeDownDistance by remember(song.id) { mutableFloatStateOf(0f) }
    var artworkZoom by remember(song.id) { mutableFloatStateOf(1f) }
    var lyricsOpening by remember(song.id) { mutableStateOf(false) }
    LaunchedEffect(song.id) {
        swipeDownDistance = 0f
        artworkZoom = 1f
        lyricsOpening = false
    }
    val scope = rememberCoroutineScope()
    val artworkLiftScale by animateFloatAsState(
        targetValue = if (lyricsOpening) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
        label = "artwork_lifts_for_lyrics"
    )
    val sheetHeight by animateDpAsState(
        targetValue = if (lyricsOpening) 210.dp else 96.dp,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "lyrics_sheet_lift"
    )
    val sheetAlpha by animateFloatAsState(if (lyricsOpening) 0.18f else 0.08f, label = "lyrics_sheet_alpha")
    fun openLyricsWithTransition() {
        if (lyricsOpening) return
        scope.launch {
            lyricsOpening = true
            delay(180)
            onNavigateToLyrics()
            lyricsOpening = false
        }
    }
    val artworkTransformState = rememberTransformableState { zoom, _, _ ->
        artworkZoom = (artworkZoom * zoom).coerceIn(1f, 2.2f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .pointerInput(song.id) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount -> if (dragAmount > 0) swipeDownDistance += dragAmount },
                    onDragEnd = {
                        if (swipeDownDistance > 150f) onNavigateBack()
                        swipeDownDistance = 0f
                    },
                    onDragCancel = { swipeDownDistance = 0f }
                )
            }
    ) {
        AsyncImage(
            model = song.albumArtUrl,
            contentDescription = "Background artwork for ${song.title}",
            modifier = Modifier
                .fillMaxSize()
                .blur(68.dp)
                .scale(1.25f),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF121212).copy(alpha = 0.65f),
                            Color(0xFF121212).copy(alpha = 0.85f),
                            Color(0xFF121212)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Minimize", tint = Color.White)
                }
                Text(
                    text = song.album.ifBlank { "Now Playing" }.uppercase(),
                    style = AppleTypography.caption1,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "Favorite", tint = Color(0xFF1DB954), modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, "Share", tint = Color.White, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = onMoreOptions) {
                    Icon(Icons.Filled.MoreVert, "More", tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }

            AsyncImage(
                model = song.albumArtUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
                    .aspectRatio(1f)
                    .scale(artworkZoom * artworkLiftScale)
                    .transformable(artworkTransformState)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(26.dp))
                    .pointerInput(song.id) {
                        detectTapGestures(onDoubleTap = { onToggleFavorite() })
                    },
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(18.dp))
            if (song.title.isBlank() || song.id.isEmpty()) {
                ErrorMessage(
                    message = "Playback unavailable: song data is missing or not loaded.",
                    onRetry = { onPlayPause(); if (!isPlaying) onPlayPause() },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = Color.White, style = AppleTypography.title2, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(song.artist, color = Color.White.copy(alpha = 0.72f), style = AppleTypography.body, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Filled.Add, "Favorite", tint = Color.White, modifier = Modifier.size(30.dp))
                }
            }

            Spacer(Modifier.height(14.dp))
            IOSProgressBar(value = progress, onSeek = onSeek)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val currentMs = (song.duration * progress).toLong()
                val totalMs = song.duration
                val fmt = { ms: Long ->
                    val m = ms / 60000
                    val s = (ms % 60000) / 1000
                    String.format("%d:%02d", m, s)
                }
                Text(fmt(currentMs), color = Color.White.copy(alpha = 0.72f), style = AppleTypography.footnote)
                Text(fmt(totalMs), color = Color.White.copy(alpha = 0.72f), style = AppleTypography.footnote)
            }
            Spacer(Modifier.height(14.dp))

            // Playback toggle controls row (shuffle/repeat) added per audit
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleShuffle, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffled) Color(0xFF1DB954) else Color.White.copy(alpha = 0.58f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onToggleRepeat, modifier = Modifier.size(44.dp)) {
                    Icon(
                        if (repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) Color(0xFF1DB954) else Color.White.copy(alpha = 0.58f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(34.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(58.dp)) { Icon(Icons.Filled.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(38.dp)) }
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1DB954).copy(alpha = 0.95f))
                        .border(1.dp, Color(0xFF1DB954), CircleShape)
                        .clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (isPlaying) "Pause" else "Play", tint = Color.Black, modifier = Modifier.size(42.dp))
                }
                IconButton(onClick = onNext, modifier = Modifier.size(58.dp)) { Icon(Icons.Filled.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(38.dp)) }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToQueue) { Icon(Icons.Filled.QueueMusic, "Queue", tint = Color.White.copy(alpha = 0.86f)) }
                IconButton(onClick = { openLyricsWithTransition() }) { Icon(Icons.Filled.Lyrics, "Lyrics", tint = Color.White.copy(alpha = 0.86f)) }
            }

            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp))
                    .background(Color(0xFF181818).copy(alpha = sheetAlpha))
                    .border(1.dp, Color(0xFF282828), RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp))
                    .clickable { openLyricsWithTransition() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.KeyboardArrowUp, null, tint = Color(0xFF1DB954).copy(alpha = 0.6f))
                    Text("Lyrics", color = Color(0xFF1DB954), style = AppleTypography.headline, fontWeight = FontWeight.Bold)
                }
            }
            }
        }
    }
}


@Composable
private fun IOSProgressBar(value: Float, onSeek: (Float) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset -> onSeek((offset.x / size.width.toFloat()).coerceIn(0f, 1f)) }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)).background(Color(0xFF1DB954).copy(alpha = 0.28f)))
        Box(Modifier.fillMaxWidth(value.coerceIn(0f, 1f)).height(7.dp).clip(RoundedCornerShape(50)).background(Color(0xFF1DB954)))
        // Scrubber thumb indicator added per audit
        Box(
            modifier = Modifier
                .offset(x = (value * 300f).dp.coerceAtMost(300.dp - 12.dp) - 6.dp)
                .align(Alignment.CenterStart)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, Color.Black.copy(alpha = 0.15f), CircleShape)
        )
    }
}
