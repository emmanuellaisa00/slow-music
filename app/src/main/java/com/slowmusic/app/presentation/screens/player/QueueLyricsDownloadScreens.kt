package com.slowmusic.app.presentation.screens.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.theme.apple.*
import com.slowmusic.app.presentation.components.apple.*

/**
 * Apple Music Style Queue Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    currentSong: Song?,
    queue: List<Song>,
    onSongClick: (Song) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
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
        // Header
        AppleNavigationBar(
            title = "Queue",
            onBackClick = onNavigateBack,
            trailing = {
                var showMenu by remember { mutableStateOf(false) }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More",
                            tint = AppleColors.textPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save as Playlist") },
                            onClick = {
                                showMenu = false
                                onSaveAsPlaylist()
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.PlaylistAdd, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Queue") },
                            onClick = {
                                showMenu = false
                                onClearQueue()
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
        )

        // Now Playing
        if (currentSong != null) {
            AppleGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                cornerRadius = 16.dp
            ) {
                Column {
                    Text(
                        text = "NOW PLAYING",
                        style = AppleTypography.caption1,
                        color = AppleColors.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = currentSong.albumArtUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 2.dp,
                                    color = AppleColors.primary,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentSong.title,
                                style = AppleTypography.headline,
                                color = AppleColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentSong.artist,
                                style = AppleTypography.subheadline,
                                color = AppleColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        PlayingIndicator()
                    }
                }
            }
        }

        // Queue header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UP NEXT",
                style = AppleTypography.caption1,
                color = AppleColors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${queue.size} songs",
                style = AppleTypography.caption1,
                color = AppleColors.textTertiary
            )
        }

        // Queue list
        if (queue.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AppleEmptyState(
                    icon = Icons.Filled.QueueMusic,
                    title = "Queue is Empty",
                    subtitle = "Add songs to your queue to see them here"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(queue) { index, song ->
                    QueueItem(
                        song = song,
                        position = index + 1,
                        onClick = { onSongClick(song) },
                        onRemove = { onRemoveFromQueue(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueItem(
    song: Song,
    position: Int,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onRemove()
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppleColors.secondary)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remove",
                    tint = Color.White
                )
            }
        },
        content = {
            AppleSongCard(
                song = song,
                onClick = onClick,
                onMoreClick = { },
                modifier = Modifier.fillMaxWidth()
            )
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    )
}

/**
 * Apple Music Style Lyrics Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    song: Song,
    lyrics: String?,
    isSynced: Boolean = false,
    onNavigateBack: () -> Unit,
    onToggleSynced: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentLine by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleColors.background)
            .statusBarsPadding()
    ) {
        // Header
        AppleNavigationBar(
            title = "Lyrics",
            onBackClick = onNavigateBack,
            trailing = {
                Switch(
                    checked = isSynced,
                    onCheckedChange = onToggleSynced,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppleColors.primary,
                        checkedTrackColor = AppleColors.primary.copy(alpha = 0.5f)
                    )
                )
            }
        )

        // Song info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArtUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = AppleTypography.headline,
                    color = AppleColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = AppleTypography.subheadline,
                    color = AppleColors.textSecondary
                )
            }
        }

        // Lyrics content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            if (lyrics != null) {
                val lines = lyrics.split("\n")

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 32.dp)
                ) {
                    itemsIndexed(lines) { index, line ->
                        val isCurrentLine = index == currentLine

                        val textSize by animateDpAsState(
                            targetValue = if (isCurrentLine) 24.dp else 18.dp,
                            animationSpec = AppleSpringAnimations.springBouncy,
                            label = "lyric_size"
                        )

                        val textAlpha by animateFloatAsState(
                            targetValue = if (isCurrentLine) 1f else 0.5f,
                            label = "lyric_alpha"
                        )

                        Text(
                            text = line.ifBlank { "♪" },
                            style = AppleTypography.body.copy(fontSize = textSize.sp),
                            color = AppleColors.textPrimary.copy(alpha = textAlpha),
                            fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentLine = index }

                        )
                    }
                }
            } else {
                // No lyrics available
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lyrics,
                            contentDescription = null,
                            tint = AppleColors.textTertiary,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Lyrics Not Available",
                            style = AppleTypography.title3,
                            color = AppleColors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "We couldn't find lyrics for this song",
                            style = AppleTypography.body,
                            color = AppleColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Apple Music Style Downloads Screen
 */
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
        // Header
        AppleNavigationBar(
            title = "Downloads",
            onBackClick = onNavigateBack,
            trailing = {
                if (downloads.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text(
                            text = "Clear All",
                            style = AppleTypography.subheadline,
                            color = AppleColors.secondary
                        )
                    }
                }
            }
        )

        // Storage info
        AppleGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            cornerRadius = 16.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Storage,
                    contentDescription = null,
                    tint = AppleColors.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Storage Used",
                        style = AppleTypography.subheadline,
                        color = AppleColors.textPrimary
                    )
                    Text(
                        text = "${downloads.size} songs",
                        style = AppleTypography.caption1,
                        color = AppleColors.textSecondary
                    )
                }

                Text(
                    text = "0 MB",
                    style = AppleTypography.headline,
                    color = AppleColors.primary
                )
            }
        }

        // Downloads list
        if (downloads.isEmpty() && downloadProgress.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AppleEmptyState(
                    icon = Icons.Filled.Download,
                    title = "No Downloads",
                    subtitle = "Download songs to listen offline"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active downloads
                if (downloadProgress.isNotEmpty()) {
                    item {
                        Text(
                            text = "DOWNLOADING",
                            style = AppleTypography.caption1,
                            color = AppleColors.textSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    downloadProgress.forEach { (songId, progress) ->
                        item {
                            DownloadProgressItem(
                                songId = songId,
                                progress = progress,
                                onCancel = { onCancelDownload(songId) }
                            )
                        }
                    }
                }

                // Completed downloads
                if (downloads.isNotEmpty()) {
                    item {
                        Text(
                            text = "DOWNLOADED",
                            style = AppleTypography.caption1,
                            color = AppleColors.textSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(downloads) { song ->
                        AppleSongCard(
                            song = song,
                            onClick = { onSongClick(song) },
                            onMoreClick = { },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadProgressItem(
    songId: String,
    progress: Float,
    onCancel: () -> Unit
) {
    AppleGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 12.dp
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(40.dp),
                    color = AppleColors.primary,
                    trackColor = AppleColors.textTertiary.copy(alpha = 0.2f),
                    strokeWidth = 3.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Downloading...",
                        style = AppleTypography.subheadline,
                        color = AppleColors.textPrimary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = AppleTypography.caption1,
                        color = AppleColors.textSecondary
                    )
                }

                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel",
                        tint = AppleColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AppleColors.primary,
                trackColor = AppleColors.textTertiary.copy(alpha = 0.2f)
            )
        }
    }
}
