package com.slowmusic.app.presentation.screens.player

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.presentation.components.apple.AppleGlassCard
import com.slowmusic.app.presentation.components.apple.AppleNavigationBar
import com.slowmusic.app.presentation.components.apple.AppleSongCard
import com.slowmusic.app.presentation.theme.apple.AppleColors
import com.slowmusic.app.presentation.theme.apple.AppleTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    currentSong: Song?,
    queue: List<Song>,
    onSongClick: (Song) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onClearQueue: () -> Unit,
    onSaveAsPlaylist: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleColors.background)
            .statusBarsPadding()
    ) {
        AppleNavigationBar(
            title = "Queue",
            onBackClick = onNavigateBack,
            trailing = {
                Row {
                    IconButton(onClick = onSaveAsPlaylist) { Icon(Icons.Filled.PlaylistAdd, "Save", tint = AppleColors.textPrimary) }
                    IconButton(onClick = onClearQueue) { Icon(Icons.Filled.Delete, "Clear", tint = AppleColors.secondary) }
                }
            }
        )

        currentSong?.let { song ->
            AppleGlassCard(modifier = Modifier.fillMaxWidth().padding(16.dp), cornerRadius = 16.dp) {
                Text("NOW PLAYING", style = AppleTypography.caption1, color = AppleColors.primary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(song.albumArtUrl, null, Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(song.title, style = AppleTypography.headline, color = AppleColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, style = AppleTypography.subheadline, color = AppleColors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(Icons.Filled.GraphicEq, null, tint = AppleColors.primary)
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("UP NEXT", style = AppleTypography.caption1, color = AppleColors.textSecondary, fontWeight = FontWeight.SemiBold)
            Text("${queue.size} songs", style = AppleTypography.caption1, color = AppleColors.textTertiary)
        }

        if (queue.isEmpty()) {
            EmptyApplePanel(Icons.Filled.QueueMusic, "Queue is Empty", "Add songs to your queue to see them here")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(queue) { index, song ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { if (index > 0) onMoveQueueItem(index, index - 1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.KeyboardArrowUp, "Move up", tint = AppleColors.textSecondary)
                            }
                            IconButton(onClick = { if (index < queue.lastIndex) onMoveQueueItem(index, index + 1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.KeyboardArrowDown, "Move down", tint = AppleColors.textSecondary)
                            }
                        }
                        AppleSongCard(
                            song = song,
                            onClick = { onSongClick(song) },
                            onMoreClick = { onRemoveFromQueue(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    song: Song,
    lyrics: String?,
    progress: Float = 0f,
    isSynced: Boolean = false,
    onNavigateBack: () -> Unit,
    onToggleSynced: (Boolean) -> Unit,
    onSeekToProgress: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var manualLine by remember { mutableStateOf<Int?>(null) }

    Box(modifier = modifier.fillMaxSize().background(AppleColors.background)) {
        AsyncImage(
            model = song.albumArtUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(60.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(0.25f), AppleColors.background.copy(0.86f), AppleColors.background)))
        )
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            AppleNavigationBar(
                title = "Lyrics",
                onBackClick = onNavigateBack
            )

            Box(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                if (lyrics != null) {
                    val parsed = remember(lyrics) { parseLrcLines(lyrics) }
                    val lines = parsed.map { it.second }
                    val currentLine = manualLine ?: currentLyricIndex(parsed, lines.size, song.duration, progress)
                    val listState = rememberLazyListState()
                    LaunchedEffect(currentLine) {
                        if (currentLine >= 0) listState.animateScrollToItem(currentLine)
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 32.dp)
                    ) {
                        itemsIndexed(lines) { index, line ->
                            val active = index == currentLine
                            val textSize by animateDpAsState(if (active) 24.dp else 18.dp, label = "lyric_size")
                            val alpha by animateFloatAsState(if (active) 1f else 0.38f, label = "lyric_alpha")
                            val scale by animateFloatAsState(
                                if (active) 1.08f else 0.96f,
                                animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
                                label = "lyric_scale"
                            )
                            Text(
                                text = line.ifBlank { "♪" },
                                style = AppleTypography.body.copy(fontSize = textSize.value.sp),
                                color = AppleColors.textPrimary.copy(alpha = alpha),
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(scale)
                                    .background(if (active) AppleColors.primary.copy(alpha = 0.10f) else Color.Transparent, RoundedCornerShape(18.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .clickable {
                                        manualLine = null
                                        onSeekToProgress(progressForLine(parsed, index, lines.size, song.duration))
                                    }
                            )
                        }
                    }
                } else {
                    EmptyApplePanel(Icons.Filled.Lyrics, "Lyrics Not Available", "We couldn't find lyrics for this song")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    downloads: List<Song>,
    downloadProgress: Map<String, Float>,
    onSongClick: (Song) -> Unit,
    onDeleteDownload: (Song) -> Unit,
    onCancelDownload: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleColors.background)
            .statusBarsPadding()
    ) {
        AppleNavigationBar(
            title = "Downloads",
            onBackClick = onNavigateBack,
            trailing = { if (downloads.isNotEmpty()) TextButton(onClick = onClearAll) { Text("Clear All", color = AppleColors.secondary) } }
        )
        AppleGlassCard(modifier = Modifier.fillMaxWidth().padding(16.dp), cornerRadius = 16.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Storage, null, tint = AppleColors.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Storage Used", style = AppleTypography.subheadline, color = AppleColors.textPrimary)
                    Text("${downloads.size} songs", style = AppleTypography.caption1, color = AppleColors.textSecondary)
                }
            }
        }
        if (downloads.isEmpty() && downloadProgress.isEmpty()) {
            EmptyApplePanel(Icons.Filled.Download, "No Downloads", "Download songs to listen offline")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                downloadProgress.forEach { (songId, p) -> item { DownloadProgressItem(songId, p, onCancel = { onCancelDownload(songId) }) } }
                items(downloads) { song -> AppleSongCard(song = song, onClick = { onSongClick(song) }, onMoreClick = { onDeleteDownload(song) }, modifier = Modifier.fillMaxWidth()) }
            }
        }
    }
}

@Composable
private fun DownloadProgressItem(songId: String, progress: Float, onCancel: () -> Unit) {
    AppleGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(progress = progress, modifier = Modifier.size(40.dp), color = AppleColors.primary, strokeWidth = 3.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Downloading...", style = AppleTypography.subheadline, color = AppleColors.textPrimary)
                    Text("${(progress * 100).toInt()}%", style = AppleTypography.caption1, color = AppleColors.textSecondary)
                }
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, "Cancel", tint = AppleColors.textSecondary) }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = AppleColors.primary)
        }
    }
}

@Composable
private fun EmptyApplePanel(icon: ImageVector, title: String, subtitle: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = AppleColors.textTertiary, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(title, style = AppleTypography.title3, color = AppleColors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = AppleTypography.body, color = AppleColors.textSecondary, textAlign = TextAlign.Center)
        }
    }
}

private fun parseLrcLines(raw: String): List<Pair<Long, String>> {
    val regex = Regex("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?]")
    return raw.lines().mapNotNull { line ->
        val match = regex.find(line)
        if (match != null) {
            val min = match.groupValues[1].toLongOrNull() ?: 0L
            val sec = match.groupValues[2].toLongOrNull() ?: 0L
            val msText = match.groupValues.getOrNull(3).orEmpty().padEnd(3, '0').take(3)
            val ms = msText.toLongOrNull() ?: 0L
            ((min * 60 + sec) * 1000 + ms) to line.replace(regex, "").trim().ifBlank { "♪" }
        } else {
            -1L to line.trim()
        }
    }.filter { it.second.isNotBlank() }
}

private fun currentLyricIndex(parsed: List<Pair<Long, String>>, size: Int, duration: Long, progress: Float): Int {
    if (size <= 0) return 0
    return if (parsed.any { it.first >= 0L }) {
        val pos = ((duration.takeIf { it > 0 } ?: 1L) * progress).toLong()
        parsed.indexOfLast { it.first in 0..pos }.coerceAtLeast(0)
    } else {
        ((size - 1) * progress).toInt().coerceIn(0, size - 1)
    }
}

private fun progressForLine(parsed: List<Pair<Long, String>>, index: Int, size: Int, duration: Long): Float {
    val stamp = parsed.getOrNull(index)?.first ?: -1L
    return if (stamp >= 0L && duration > 0) (stamp.toFloat() / duration).coerceIn(0f, 1f)
    else (index.toFloat() / (size - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
}
