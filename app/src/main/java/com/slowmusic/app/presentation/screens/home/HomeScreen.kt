package com.slowmusic.app.presentation.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.presentation.components.*
import com.slowmusic.app.presentation.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onGenreClick: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSeeAll: (String) -> Unit,
    onAddToPlaylist: (Song) -> Unit = {},
    onPlayNext: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onDownload: (Song) -> Unit = {},
    onShare: (Song) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadContent()
    }
    
    selectedSong?.let { song ->
        SongOptionsBottomSheet(
            song = song,
            onDismiss = { selectedSong = null },
            onAddToPlaylist = { onAddToPlaylist(song); selectedSong = null },
            onPlayNext = { onPlayNext(song); selectedSong = null },
            onAddToQueue = { onAddToQueue(song); selectedSong = null },
            onDownload = { onDownload(song); selectedSong = null },
            onShare = { onShare(song); selectedSong = null },
            onGoToArtist = { onArtistClick(song.artist); selectedSong = null },
            onGoToAlbum = { selectedSong = null }
        )
    }

    Scaffold(
    ) { paddingValues ->
        var pullDistance by remember { mutableFloatStateOf(0f) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.isRefreshing) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount > 0) pullDistance += dragAmount
                        },
                        onDragEnd = {
                            if (pullDistance > 120f && !uiState.isRefreshing) viewModel.refresh()
                            pullDistance = 0f
                        },
                        onDragCancel = { pullDistance = 0f }
                    )
                }
        ) {
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null -> ErrorMessage(
                message = uiState.error!!,
                onRetry = { viewModel.loadContent() }
            )
                else -> HomeContent(
                    uiState = uiState,
                    onSongClick = onSongClick,
                    onMoreClick = { selectedSong = it },
                    onArtistClick = onArtistClick,
                    onAlbumClick = onAlbumClick,
                    onGenreClick = onGenreClick,
                    onSeeAllClick = onNavigateToSeeAll,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToNotifications = onNavigateToNotifications,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            if (uiState.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(androidx.compose.ui.Alignment.TopCenter))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onSongClick: (Song, List<Song>) -> Unit,
    onMoreClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onGenreClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val heroCollapse by remember { derivedStateOf { (listState.firstVisibleItemScrollOffset / 280f).coerceIn(0f, 1f) } }
    val featuredSong = remember(uiState) {
        uiState.recentlyPlayed.firstOrNull()
            ?: uiState.trendingSongs.firstOrNull()
            ?: uiState.topSongs.firstOrNull()
            ?: uiState.recommendations.firstOrNull()
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 112.dp)
    ) {
        item {
            PremiumHomeHero(
                loadedFromCache = uiState.loadedFromCache,
                collapse = heroCollapse,
                onSearch = onNavigateToSearch,
                onNotifications = onNavigateToNotifications
            )
        }

        featuredSong?.let { song ->
            item {
                HomeSectionHeader(title = "Featured for you", onSeeAll = { onSeeAllClick("featured") })
                FeaturedSongCard(
                    song = song,
                    queue = listOf(song) + uiState.trendingSongs + uiState.topSongs,
                    onSongClick = onSongClick,
                    onMoreClick = onMoreClick
                )
            }
        }

        if (uiState.recentlyPlayed.isNotEmpty()) {
            item { HomeSectionHeader(title = "Continue listening", onSeeAll = { onSeeAllClick("recent") }) }
            item {
                QuickPicksRow(
                    songs = uiState.recentlyPlayed.take(8),
                    onSongClick = onSongClick,
                    onMoreClick = onMoreClick
                )
            }
        }

        if (uiState.genres.isNotEmpty()) {
            item { HomeSectionHeader(title = "Browse by Genre") }
            item {
                GenreRow(
                    genres = uiState.genres,
                    onGenreClick = onGenreClick
                )
            }
        }

        if (uiState.trendingSongs.isNotEmpty()) {
            item { HomeSectionHeader(title = "Trending Now", onSeeAll = { onSeeAllClick("trending") }) }
            item {
                SongRow(
                    songs = uiState.trendingSongs,
                    onSongClick = onSongClick,
                    onMoreClick = onMoreClick
                )
            }
        }

        if (uiState.topSongs.isNotEmpty()) {
            item { HomeSectionHeader(title = "Top Hits", onSeeAll = { onSeeAllClick("top") }) }
            item {
                SongRow(
                    songs = uiState.topSongs,
                    onSongClick = onSongClick,
                    onMoreClick = onMoreClick
                )
            }
        }

        if (uiState.newReleases.isNotEmpty()) {
            item { HomeSectionHeader(title = "New Releases", onSeeAll = { onSeeAllClick("new") }) }
            item {
                AlbumRow(
                    albums = uiState.newReleases,
                    onAlbumClick = onAlbumClick
                )
            }
        }

        if (uiState.recommendations.isNotEmpty()) {
            item { HomeSectionHeader(title = "Recommended For You", onSeeAll = { onSeeAllClick("recommended") }) }
            item {
                SongRow(
                    songs = uiState.recommendations,
                    onSongClick = onSongClick,
                    onMoreClick = onMoreClick
                )
            }
        }
    }
}

@Composable
private fun PremiumHomeHero(
    loadedFromCache: Boolean,
    collapse: Float,
    onSearch: () -> Unit,
    onNotifications: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .graphicsLayer {
                translationY = -collapse * 12f
                alpha = 1f - collapse * 0.10f
                scaleX = 1f - collapse * 0.015f
                scaleY = 1f - collapse * 0.015f
            }
            .shadow(14.dp, RoundedCornerShape(28.dp), clip = false),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 132.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            PrimaryGreen.copy(alpha = 0.46f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 108.dp)
            ) {
                Text(
                    "Slow Music",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (loadedFromCache) "Instant from cache • pull down to refresh" else "Fresh picks, full songs, local music",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                FilledTonalButton(
                    onClick = onSearch,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.16f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Filled.Search, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Find music", fontWeight = FontWeight.SemiBold)
                }
            }
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroIconButton(Icons.Filled.Notifications, "Notifications", onNotifications)
                HeroIconButton(Icons.Filled.Search, "Search", onSearch)
            }
        }
    }
}

@Composable
private fun HeroIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.32f))
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun HomeSectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                Text("See all", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun FeaturedSongCard(
    song: Song,
    queue: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit,
    onMoreClick: (Song) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(10.dp, RoundedCornerShape(24.dp), clip = false)
            .clickable { onSongClick(song, queue) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.86f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 5.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = song.albumArtUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .size(96.dp)
                    .shadow(6.dp, RoundedCornerShape(18.dp), clip = false)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text("FEATURED", style = MaterialTheme.typography.labelSmall, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(song.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = { onSongClick(song, queue) },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Play", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = { onMoreClick(song) }, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = "More")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPicksRow(
    songs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit,
    onMoreClick: (Song) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(songs) { song ->
            QuickPickCard(
                song = song,
                onClick = { onSongClick(song, songs) },
                onMoreClick = { onMoreClick(song) }
            )
        }
    }
}

@Composable
private fun QuickPickCard(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    com.slowmusic.app.presentation.components.SongCard(
        song = song,
        onClick = onClick,
        onMoreClick = onMoreClick
    )
}

@Composable
private fun GenreRow(
    genres: List<com.slowmusic.app.domain.model.Genre>,
    onGenreClick: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(genres) { genre ->
            HomeGenreChip(
                name = genre.name,
                onClick = { onGenreClick(genre.id) }
            )
        }
    }
}


@Composable
private fun HomeGenreChip(name: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f),
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SongRow(
    songs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit,
    onMoreClick: (Song) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(songs) { song ->
            SongCard(
                song = song,
                onClick = { onSongClick(song, songs) },
                onMoreClick = { onMoreClick(song) }
            )
        }
    }
}

@Composable
private fun AlbumRow(
    albums: List<com.slowmusic.app.domain.model.Album>,
    onAlbumClick: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(albums) { album ->
            AlbumCard(
                title = album.title,
                artist = album.artist,
                artworkUrl = album.artworkUrl,
                onClick = { onAlbumClick(album.id) }
            )
        }
    }
}
