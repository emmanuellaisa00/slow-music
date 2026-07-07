@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
    onSongClick: (Song, List<Song>) -> Unit,
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
    onAddToPlaylist: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onDownload: (Song) -> Unit = {},
    onShare: (Song) -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val followedArtists by viewModel.followedArtists.collectAsState()
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    selectedSong?.let { song ->
        SongOptionsBottomSheet(
            song = song,
            onDismiss = { selectedSong = null },
            onAddToPlaylist = { onAddToPlaylist(song); selectedSong = null },
            onAddToQueue = { onAddToQueue(song); selectedSong = null },
            onDownload = { onDownload(song); selectedSong = null },
            onShare = { onShare(song); selectedSong = null },
            onGoToArtist = { selectedSong = null },
            onGoToAlbum = { selectedSong = null }
        )
    }

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
                    icon = Icons.Filled.KeyboardArrowRight,
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
                    icon = Icons.Filled.KeyboardArrowRight,
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
                    icon = Icons.Filled.KeyboardArrowRight,
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
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
fun FavoritesScreen(
    onSongClick: (Song, List<Song>) -> Unit,
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
                        onClick = { onSongClick(song, favorites) },
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
    onSongClick: (Song, List<Song>) -> Unit,
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
                        onClick = { onSongClick(song, recentPlays) },
                        onMoreClick = { }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadsScreen(
    onSongClick: (Song, List<Song>) -> Unit,
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
                        onClick = { onSongClick(song, downloads) },
                        onMoreClick = { }
                    )
                }
            }
        }
    }
}

@Composable
fun MostPlayedScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    viewModel: MostPlayedViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Most Played") }) }) { padding ->
        if (songs.isEmpty()) EmptyState(icon = { Icon(Icons.Filled.TrendingUp, null, Modifier.size(64.dp)) }, title = "No plays yet", subtitle = "Play songs to build your chart", modifier = Modifier.padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 100.dp)) {
            items(songs) { song -> SongListItem(song, { onSongClick(song, songs) }, {}) }
        }
    }
}

@Composable
fun LocalMusicScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    viewModel: LocalMusicViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    LaunchedEffect(Unit) { viewModel.scan() }
    Scaffold(topBar = { TopAppBar(title = { Text("Local Music") }, actions = { IconButton(onClick = { viewModel.scan() }) { Icon(Icons.Filled.Refresh, "Rescan") } }) }) { padding ->
        if (songs.isEmpty()) EmptyState(icon = { Icon(Icons.Filled.Smartphone, null, Modifier.size(64.dp)) }, title = "No local songs found", subtitle = "Grant audio permission and tap rescan", modifier = Modifier.padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 100.dp)) {
            items(songs) { song -> SongListItem(song, { onSongClick(song, songs) }, {}) }
        }
    }
}

@Composable
fun PlaylistsScreen(
    onPlaylistClick: (String) -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    if (showCreate) AlertDialog(onDismissRequest = { showCreate = false }, title = { Text("Create playlist") }, text = { OutlinedTextField(name, { name = it }, label = { Text("Name") }) }, confirmButton = { TextButton(onClick = { viewModel.create(name.ifBlank { "New Playlist" }); name = ""; showCreate = false }) { Text("Create") } }, dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } })
    Scaffold(topBar = { TopAppBar(title = { Text("Playlists") }, actions = { IconButton(onClick = { showCreate = true }) { Icon(Icons.Filled.Add, "Create") } }) }) { padding ->
        if (playlists.isEmpty()) EmptyState(icon = { Icon(Icons.Filled.QueueMusic, null, Modifier.size(64.dp)) }, title = "No playlists", subtitle = "Create your first playlist", modifier = Modifier.padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 100.dp)) {
            items(playlists) { playlist ->
                ListItem(modifier = Modifier.clickable { onPlaylistClick(playlist.id) }, leadingContent = { Icon(Icons.Filled.QueueMusic, null) }, headlineContent = { Text(playlist.name) }, supportingContent = { Text("${playlist.songIds.size} songs") }, trailingContent = { IconButton(onClick = { viewModel.delete(playlist.id) }) { Icon(Icons.Filled.Delete, "Delete") } })
            }
        }
    }
}

@Composable
fun FollowedArtistsScreen(
    onArtistClick: (String) -> Unit,
    viewModel: FollowedArtistsViewModel = hiltViewModel()
) {
    val artists by viewModel.artists.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Followed Artists") }) }) { padding ->
        if (artists.isEmpty()) EmptyState(icon = { Icon(Icons.Filled.Person, null, Modifier.size(64.dp)) }, title = "No followed artists", subtitle = "Follow artists from their profile", modifier = Modifier.padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 100.dp)) {
            items(artists) { artist -> ListItem(modifier = Modifier.clickable { onArtistClick(artist.id) }, leadingContent = { Icon(Icons.Filled.Person, null) }, headlineContent = { Text(artist.name) }, supportingContent = { Text("${artist.songCount} songs") }, trailingContent = { TextButton(onClick = { viewModel.unfollow(artist.id) }) { Text("Unfollow") } }) }
        }
    }
}

@Composable
fun SavedAlbumsScreen(
    onAlbumClick: (String) -> Unit,
    viewModel: SavedAlbumsViewModel = hiltViewModel()
) {
    val albums by viewModel.albums.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Saved Albums") }) }) { padding ->
        if (albums.isEmpty()) EmptyState(icon = { Icon(Icons.Filled.Album, null, Modifier.size(64.dp)) }, title = "No saved albums", subtitle = "Albums from favorites/downloads appear here", modifier = Modifier.padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 100.dp)) {
            items(albums) { album -> ListItem(modifier = Modifier.clickable { onAlbumClick(album.id) }, leadingContent = { Icon(Icons.Filled.Album, null) }, headlineContent = { Text(album.title) }, supportingContent = { Text("${album.artist} • ${album.trackCount} songs") }, trailingContent = { Icon(Icons.Filled.KeyboardArrowRight, null) }) }
        }
    }
}
