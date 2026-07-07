package com.slowmusic.app.presentation.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.components.*
import com.slowmusic.app.presentation.navigation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSongClick: (Song) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToRecent: () -> Unit,
    onNavigateToMostPlayed: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToLocalMusic: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onPlaylistClick: (String) -> Unit = {},
    onNavigateToArtists: () -> Unit,
    onNavigateToAlbums: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val followedArtists by viewModel.followedArtists.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Library Sections
            item {
                LibrarySection(title = "Library")
            }
            
            item {
                LibraryListItem(
                    icon = Icons.Filled.Favorite,
                    title = "Favorites",
                    subtitle = "${favorites.size} songs",
                    onClick = onNavigateToFavorites
                )
            }
            
            item {
                LibraryListItem(
                    icon = Icons.Filled.History,
                    title = "Recently Played",
                    subtitle = "Your listening history",
                    onClick = onNavigateToRecent
                )
            }
            
            item {
                LibraryListItem(
                    icon = Icons.Filled.TrendingUp,
                    title = "Most Played",
                    subtitle = "Your top songs",
                    onClick = onNavigateToMostPlayed
                )
            }
            
            item {
                LibraryListItem(
                    icon = Icons.Filled.Download,
                    title = "Downloads",
                    subtitle = "${downloadedSongs.size} songs",
                    onClick = onNavigateToDownloads
                )
            }
            
            item {
                LibraryListItem(
                    icon = Icons.Filled.Smartphone,
                    title = "Local Music",
                    subtitle = "Music on your device",
                    onClick = onNavigateToLocalMusic
                )
            }
            
            // Playlists Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                LibrarySection(title = "Playlists")
            }
            
            item {
                LibraryListItem(
                    icon = Icons.Filled.Add,
                    title = "Create Playlist",
                    subtitle = "Make your own playlist",
                    onClick = { viewModel.createPlaylist() }
                )
            }
            
            items(playlists) { playlist ->
                LibraryListItem(
                    icon = Icons.Filled.QueueMusic,
                    title = playlist.name,
                    subtitle = "${playlist.songIds.size} songs",
                    onClick = { onPlaylistClick(playlist.id) }
                )
            }
            
            item {
                LibraryListItem(
                    icon = Icons.Filled.ChevronRight,
                    title = "See all playlists",
                    subtitle = "",
                    onClick = onNavigateToPlaylists
                )
            }
            
            // Artists Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                LibrarySection(title = "Artists")
            }
            
            item {
                LibraryListItem(
                    icon = Icons.Filled.ChevronRight,
                    title = "Following",
                    subtitle = "${followedArtists.size} artists",
                    onClick = onNavigateToArtists
                )
            }
            
            // Albums Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                LibrarySection(title = "Albums")
            }
            
            item {
                LibraryListItem(
                    icon = Icons.Filled.ChevronRight,
                    title = "Your Albums",
                    subtitle = "Albums you've saved",
                    onClick = onNavigateToAlbums
                )
            }
        }
    }
}

@Composable
private fun LibrarySection(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun LibraryListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
fun FavoritesScreen(
    onSongClick: (Song) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Favorites") })
        }
    ) { paddingValues ->
        if (favorites.isEmpty()) {
            EmptyState(
                icon = {
                    Icon(
                        Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                },
                title = "No favorites yet",
                subtitle = "Songs you like will appear here",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(favorites) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onMoreClick = { },
                        onFavoriteClick = { viewModel.removeFromFavorites(song) },
                        isFavorite = true
                    )
                }
            }
        }
    }
}

@Composable
fun RecentPlaysScreen(
    onSongClick: (Song) -> Unit,
    viewModel: RecentlyPlayedViewModel = hiltViewModel()
) {
    val recentPlays by viewModel.recentPlays.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recently Played") },
                actions = {
                    TextButton(onClick = { viewModel.clearHistory() }) {
                        Text("Clear")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (recentPlays.isEmpty()) {
            EmptyState(
                icon = {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                },
                title = "No recent plays",
                subtitle = "Songs you've played will appear here",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(recentPlays) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onMoreClick = { }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadsScreen(
    onSongClick: (Song) -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val downloads by viewModel.downloads.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Downloads") })
        }
    ) { paddingValues ->
        if (downloads.isEmpty()) {
            EmptyState(
                icon = {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                },
                title = "No downloads",
                subtitle = "Downloaded songs will appear here",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(downloads) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onMoreClick = { }
                    )
                }
            }
        }
    }
}
