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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
            .background(Color(0xFF082737))
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
            contentDescription = null,
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
                        listOf(
                            Color(0xCC082737),
                            Color(0xEE062231),
                            Color(0xFF061D2A)
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
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.size(48.dp))
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

            Spacer(Modifier.height(24.dp))

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

            Spacer(Modifier.height(20.dp))
            IOSProgressBar(value = progress, onSeek = onSeek)
            Spacer(Modifier.height(26.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(34.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(58.dp)) { Icon(Icons.Filled.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(38.dp)) }
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                        .clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (isPlaying) "Pause" else "Play", tint = Color.White, modifier = Modifier.size(42.dp))
                }
                IconButton(onClick = onNext, modifier = Modifier.size(58.dp)) { Icon(Icons.Filled.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(38.dp)) }
            }

            Spacer(Modifier.height(20.dp))
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
                    .background(Color.White.copy(alpha = sheetAlpha))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp))
                    .clickable { openLyricsWithTransition() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.KeyboardArrowUp, null, tint = Color.White.copy(alpha = 0.22f))
                    Text("Lyrics", color = Color.White, style = AppleTypography.headline, fontWeight = FontWeight.Bold)
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
        Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.22f)))
        Box(Modifier.fillMaxWidth(value.coerceIn(0f, 1f)).height(7.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.86f)))
    }
}
