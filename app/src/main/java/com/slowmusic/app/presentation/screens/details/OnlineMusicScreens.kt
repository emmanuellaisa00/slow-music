@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.slowmusic.app.presentation.screens.details

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.data.repository.ContentCacheRepository
import com.slowmusic.app.data.repository.DownloadManager
import com.slowmusic.app.data.repository.DownloadState
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.repository.MusicRepository
import com.slowmusic.app.presentation.components.PremiumLockedHeader
import com.slowmusic.app.streaming.StreamingFallbackResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailsScreen(
    artistId: String,
    onNavigateBack: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    viewModel: ArtistDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(artistId) { viewModel.load(artistId) }

    Scaffold(topBar = { TopAppBar(title = { Text(state.artist?.name ?: "Artist") }, navigationIcon = { BackButton(onNavigateBack) }) }) { padding ->
        val listState = rememberLazyListState()
        val lockActive by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 8 } }
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
            item {
                HeroHeader(
                    title = state.artist?.name ?: "Artist",
                    subtitle = "${state.songs.size} songs • ${state.albums.size} albums",
                    icon = Icons.Filled.Person,
                    action = {
                        Button(onClick = { state.artist?.let(viewModel::toggleFollow) }) {
                            Icon(if (state.isFollowing) Icons.Filled.Check else Icons.Filled.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.isFollowing) "Following" else "Follow")
                        }
                    }
                )
            }
            stickyHeader { PremiumLockedHeader("Top Songs", active = lockActive) }
            items(state.songs.take(10)) { song -> SongRow(song, { onSongClick(song, state.songs) }) }
            stickyHeader { PremiumLockedHeader("Albums & Singles", active = lockActive) }
            items(state.albums) { album ->
                ListItem(
                    modifier = Modifier.clickable { onAlbumClick(album.id) },
                    leadingContent = {
                        AsyncImage(
                            model = album.artworkUrl,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    },
                    headlineContent = { Text(album.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = { Text(album.releaseDate ?: album.artist) },
                    trailingContent = { Icon(Icons.Filled.KeyboardArrowRight, null) }
                )
            }
            stickyHeader { PremiumLockedHeader("About", active = lockActive) }
            item { Text("Follow ${state.artist?.name ?: "this artist"} to keep their new releases and top songs in your library. Artist biographies and verified metadata can be enhanced as more music data is discovered.") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailsScreen(
    albumId: String,
    onNavigateBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    viewModel: AlbumDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(albumId) { viewModel.load(albumId) }
    val album = state.album

    Scaffold(
        containerColor = Color(0xFF0B0B0B),
        topBar = {
            TopAppBar(
                title = { Text(album?.title ?: "Album", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { BackButton(onNavigateBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        val lockActive by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 8 } }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                SpotifyBlurHeader(
                    title = album?.title ?: "Album",
                    subtitle = listOfNotNull(album?.artist, album?.releaseDate, album?.genre).joinToString(" • ").ifBlank { "Album" },
                    artworkUrl = album?.artworkUrl,
                    fallbackIcon = Icons.Filled.Album,
                    typeLabel = "Album",
                    primaryButtonText = "Play",
                    onPrimaryClick = { state.songs.firstOrNull()?.let { onSongClick(it, state.songs) } },
                    onSecondaryClick = viewModel::saveAlbum
                )
            }

            stickyHeader { SpotifySectionTitle("Tracks", active = lockActive) }
            if (state.songs.isEmpty()) {
                item { SpotifyEmptyRow("No tracks found", "Tracks from this album will appear here.") }
            }
            items(state.songs) { song ->
                SpotifyTrackRow(
                    song = song,
                    index = state.songs.indexOf(song) + 1,
                    onClick = { onSongClick(song, state.songs) },
                    onMore = { onAddToPlaylist(song) }
                )
            }
            item {
                Text(
                    text = "Released by ${album?.artist ?: "Unknown label"}. Track metadata is cached locally for faster library use.",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailsScreen(
    playlistId: String,
    onNavigateBack: () -> Unit,
    onAddSongs: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    viewModel: PlaylistDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var editing by remember { mutableStateOf(false) }
    var newName by remember(state.playlist?.name) { mutableStateOf(state.playlist?.name.orEmpty()) }
    LaunchedEffect(playlistId) { viewModel.load(playlistId) }

    if (editing) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Edit playlist") },
            text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") }, singleLine = true) },
            confirmButton = { TextButton(onClick = { viewModel.rename(newName); editing = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { editing = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0B0B0B),
        topBar = {
            TopAppBar(
                title = { Text(state.playlist?.name ?: "Playlist", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { BackButton(onNavigateBack) },
                actions = { IconButton(onClick = { editing = true }) { Icon(Icons.Filled.Edit, "Edit", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        val lockActive by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 8 } }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                SpotifyBlurHeader(
                    title = state.playlist?.name ?: "Playlist",
                    subtitle = "${state.songs.size} songs • Local database",
                    artworkUrl = state.songs.firstOrNull()?.albumArtUrl ?: state.playlist?.artworkUrl,
                    fallbackIcon = Icons.Filled.QueueMusic,
                    typeLabel = "Playlist",
                    primaryButtonText = "Play",
                    onPrimaryClick = { state.songs.firstOrNull()?.let { onSongClick(it, state.songs) } },
                    secondaryButtonText = "Add songs",
                    onSecondaryClick = onAddSongs
                )
            }

            stickyHeader { SpotifySectionTitle("Songs", active = lockActive) }
            if (state.songs.isEmpty()) {
                item { SpotifyEmptyRow("No songs yet", "Use Add songs from album/search menus to build this playlist.") }
            }
            items(state.songs) { song ->
                SpotifyTrackRow(
                    song = song,
                    index = state.songs.indexOf(song) + 1,
                    onClick = { onSongClick(song, state.songs) },
                    onMore = { viewModel.removeSong(song.id) },
                    moreIcon = Icons.Filled.RemoveCircleOutline
                )
            }
        }
    }
}

@Composable
private fun SpotifyBlurHeader(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    typeLabel: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String = "Save",
    onSecondaryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp)
    ) {
        AsyncImage(
            model = artworkUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .blur(34.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.18f),
                            Color(0xFF121212).copy(alpha = 0.78f),
                            Color(0xFF0B0B0B)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(176.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF4F4F4F), Color(0xFF191919)))),
                contentAlignment = Alignment.Center
            ) {
                if (artworkUrl != null) {
                    AsyncImage(
                        model = artworkUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(fallbackIcon, null, tint = Color.White.copy(alpha = 0.86f), modifier = Modifier.size(76.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(typeLabel, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onSecondaryClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White
                    )
                ) { Text(secondaryButtonText) }
                Spacer(Modifier.weight(1f))
                FloatingActionButton(
                    onClick = onPrimaryClick,
                    containerColor = Color(0xFF1DB954),
                    contentColor = Color.Black,
                    shape = CircleShape
                ) { Icon(Icons.Filled.PlayArrow, primaryButtonText, modifier = Modifier.size(34.dp)) }
            }
        }
    }
}

@Composable
private fun SpotifySectionTitle(text: String, active: Boolean = false) {
    PremiumLockedHeader(title = text, active = active, dark = true)
}

@Composable
private fun SpotifyTrackRow(
    song: Song,
    index: Int,
    onClick: () -> Unit,
    onMore: () -> Unit,
    moreIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.MoreVert
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(index.toString(), color = Color.White.copy(alpha = 0.55f), modifier = Modifier.width(24.dp))
                AsyncImage(
                    model = song.albumArtUrl,
                    contentDescription = null,
                    modifier = Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        },
        headlineContent = { Text(song.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(song.artist, color = Color.White.copy(alpha = 0.58f), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingContent = { IconButton(onClick = onMore) { Icon(moreIcon, null, tint = Color.White.copy(alpha = 0.72f)) } }
    )
}

@Composable
private fun SpotifyEmptyRow(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = Color.White.copy(alpha = 0.62f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistScreen(
    songId: String,
    title: String,
    artist: String,
    album: String,
    onNavigateBack: () -> Unit,
    viewModel: AddToPlaylistViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    if (showCreate) AlertDialog(
        onDismissRequest = { showCreate = false },
        title = { Text("Create playlist") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Playlist name") }) },
        confirmButton = { TextButton(onClick = { viewModel.createPlaylist(name.ifBlank { "New Playlist" }); showCreate = false; name = "" }) { Text("Create") } },
        dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
    )
    Scaffold(topBar = { TopAppBar(title = { Text("Add to playlist") }, navigationIcon = { BackButton(onNavigateBack) }, actions = { IconButton(onClick = { showCreate = true }) { Icon(Icons.Filled.Add, "Create") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
            item { Text("Choose where to save $title by $artist", style = MaterialTheme.typography.bodyMedium) }
            items(playlists) { playlist ->
                ListItem(
                    modifier = Modifier.clickable { viewModel.addSong(Song(songId, title, artist, album, null, null, null, 0, null, null), playlist.id); onNavigateBack() },
                    leadingContent = { Icon(Icons.Filled.QueueMusic, null, tint = MaterialTheme.colorScheme.primary) },
                    headlineContent = { Text(playlist.name) },
                    supportingContent = { Text("${playlist.songIds.size} songs") },
                    trailingContent = { Icon(Icons.Filled.KeyboardArrowRight, null) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastDevicePickerScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val castContext = remember { runCatching { com.google.android.gms.cast.framework.CastContext.getSharedInstance(context) }.getOrNull() }
    val session = castContext?.sessionManager?.currentCastSession
    val connectedName = session?.castDevice?.friendlyName
    Scaffold(topBar = { TopAppBar(title = { Text("Connect to device") }, navigationIcon = { BackButton(onNavigateBack) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
            item { Text("Cast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Filled.Cast, null, tint = MaterialTheme.colorScheme.primary) },
                    headlineContent = { Text(connectedName ?: "No active Cast session") },
                    supportingContent = { Text(if (connectedName == null) "Use the system Cast picker from the player/device menu to connect." else "Connected and ready for remote playback handoff.") },
                    trailingContent = { if (connectedName != null) TextButton(onClick = { castContext.sessionManager.endCurrentSession(true) }) { Text("Disconnect") } }
                )
            }
            item { AssistChip(onClick = {}, label = { Text("Google Cast framework is initialized. Device discovery appears when Google Play services exposes routes on device.") }, leadingIcon = { Icon(Icons.Filled.Info, null) }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadStorageManagerScreen(onNavigateBack: () -> Unit, viewModel: DownloadStorageViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }
    Scaffold(topBar = { TopAppBar(title = { Text("Download storage") }, navigationIcon = { BackButton(onNavigateBack) }, actions = { IconButton(onClick = viewModel::refresh) { Icon(Icons.Filled.Refresh, "Refresh") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
            item { StorageCard(state.storageUsed, state.downloads.size) }
            item {
                CacheCard(
                    cacheBytes = state.cacheUsed,
                    onClearMetadata = { viewModel.clearMetadataCache() },
                    onClearRuntime = { viewModel.clearRuntimeCaches() }
                )
            }
            if (activeDownloads.isNotEmpty()) {
                item { SectionTitle("Active downloads") }
                items(activeDownloads.entries.toList()) { entry ->
                    val state = entry.value
                    val progress = (state as? DownloadState.Downloading)?.progress ?: 0f
                    ListItem(
                        headlineContent = { Text((state as? DownloadState.Failed)?.song?.title ?: entry.key) },
                        supportingContent = {
                            if (state is DownloadState.Failed) Text(state.error, color = MaterialTheme.colorScheme.error)
                            else LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                        },
                        trailingContent = {
                            if (state is DownloadState.Failed) TextButton(onClick = { viewModel.retry(entry.key) }) { Text("Retry") }
                            else TextButton(onClick = { viewModel.cancel(entry.key) }) { Text("Cancel") }
                        }
                    )
                }
            }
            item { SectionTitle("Downloaded songs") }
            if (state.downloads.isEmpty()) item { EmptyPanel("No downloads", "Offline songs and failed downloads will appear here.") }
            items(state.downloads) { song ->
                ListItem(headlineContent = { Text(song.title) }, supportingContent = { Text(song.artist) }, leadingContent = { Icon(Icons.Filled.DownloadDone, null, tint = MaterialTheme.colorScheme.primary) }, trailingContent = { IconButton(onClick = { viewModel.delete(song) }) { Icon(Icons.Filled.Delete, "Delete") } })
            }
            item { OutlinedButton(onClick = viewModel::clearAll, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.DeleteSweep, null); Spacer(Modifier.width(8.dp)); Text("Remove all downloaded files") } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalTextScreen(title: String, body: String, onNavigateBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { BackButton(onNavigateBack) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
            item { Text(body, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionExplainerScreen(title: String, description: String, permission: String, onNavigateBack: () -> Unit) {
    var resultText by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        resultText = if (granted) "Permission granted" else "Permission denied. You can enable it later in Android Settings."
    }
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { BackButton(onNavigateBack) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Filled.Security, null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
            resultText?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(24.dp))
            Button(onClick = { launcher.launch(permission) }) { Text("Grant ${permissionLabel(permission)}") }
            TextButton(onClick = onNavigateBack) { Text("Not now") }
        }
    }
}

private fun permissionLabel(permission: String): String = when (permission) {
    Manifest.permission.POST_NOTIFICATIONS -> "notifications"
    Manifest.permission.READ_MEDIA_AUDIO -> "music access"
    else -> "permission"
}

@HiltViewModel
class ArtistDetailsViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val libraryRepository: LibraryRepository,
    private val streamingFallbackResolver: StreamingFallbackResolver
) : ViewModel() {
    private val _state = MutableStateFlow(ArtistDetailsState())
    val state: StateFlow<ArtistDetailsState> = _state.asStateFlow()
    fun load(id: String) = viewModelScope.launch {
        val baseArtist = musicRepository.getArtistById(id)
        val artistName = baseArtist?.name ?: id.replace('_', ' ').replaceFirstChar { it.uppercase() }
        val catalogSongs = runCatching { musicRepository.getSongsByArtist(id) }.getOrDefault(emptyList())
        val fallbackSongs = runCatching { streamingFallbackResolver.searchSongs(artistName, 20) }.getOrDefault(emptyList())
        val songs = (catalogSongs + fallbackSongs).distinctBy { it.title.lowercase() to it.artist.lowercase() }
        val discoveredAlbums = (runCatching { musicRepository.searchAlbums(artistName) }.getOrDefault(emptyList()) +
            runCatching { streamingFallbackResolver.searchAlbums(artistName) }.getOrDefault(emptyList()))
        val derivedAlbums = songs
            .filter { it.album.isNotBlank() }
            .groupBy { it.album.lowercase() to it.artist.lowercase() }
            .map { (_, albumSongs) ->
                val first = albumSongs.first()
                Album(
                    id = "artist_${artistName}_${first.album}".replace(" ", "_"),
                    title = first.album,
                    artist = first.artist,
                    artistId = first.artist.hashCode().toString(),
                    artworkUrl = first.albumArtUrl,
                    trackCount = albumSongs.size,
                    releaseDate = first.releaseDate,
                    genre = first.genre
                )
            }
        val albums = (discoveredAlbums + derivedAlbums)
            .distinctBy { it.title.lowercase() to it.artist.lowercase() }
        val artist = baseArtist ?: Artist(
            id = id,
            name = artistName,
            imageUrl = songs.firstOrNull { !it.albumArtUrl.isNullOrBlank() }?.albumArtUrl,
            genre = songs.firstOrNull()?.genre,
            albumCount = albums.size,
            songCount = songs.size
        )
        val following = libraryRepository.isFollowing(id)
        _state.value = ArtistDetailsState(artist, songs, albums, following)
    }
    fun toggleFollow(artist: Artist) = viewModelScope.launch {
        if (_state.value.isFollowing) libraryRepository.unfollowArtist(artist.id) else libraryRepository.followArtist(artist)
        _state.update { it.copy(isFollowing = !it.isFollowing) }
    }
}

data class ArtistDetailsState(val artist: Artist? = null, val songs: List<Song> = emptyList(), val albums: List<Album> = emptyList(), val isFollowing: Boolean = false)

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val libraryRepository: LibraryRepository,
    private val streamingFallbackResolver: StreamingFallbackResolver
) : ViewModel() {
    private val _state = MutableStateFlow(AlbumDetailsState())
    val state: StateFlow<AlbumDetailsState> = _state.asStateFlow()
    fun load(id: String) = viewModelScope.launch {
        if (id.startsWith("ytalbum_")) {
            val songs = streamingFallbackResolver.playlistSongs(id)
            val first = songs.firstOrNull()
            val album = Album(
                id = id,
                title = first?.album ?: "Album",
                artist = first?.artist ?: "Music",
                artistId = (first?.artist ?: id).hashCode().toString(),
                artworkUrl = first?.albumArtUrl,
                trackCount = songs.size,
                releaseDate = null,
                genre = null
            )
            _state.value = AlbumDetailsState(album, songs)
        } else {
            val album = musicRepository.getAlbumById(id) ?: Album(id, "Album $id", "Unknown Artist", "", null, 0, null, null)
            _state.value = AlbumDetailsState(album, musicRepository.getSongsByAlbum(id))
        }
    }
    fun saveAlbum() = viewModelScope.launch { _state.value.songs.take(1).forEach { libraryRepository.addToFavorites(it) } }
}

data class AlbumDetailsState(val album: Album? = null, val songs: List<Song> = emptyList())

@HiltViewModel
class PlaylistDetailsViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val streamingFallbackResolver: StreamingFallbackResolver,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val playlistId: String = savedStateHandle["playlistId"] ?: ""
    private val _state = MutableStateFlow(PlaylistDetailsState())
    val state: StateFlow<PlaylistDetailsState> = _state.asStateFlow()
    fun load(id: String = playlistId) = viewModelScope.launch {
        if (id.startsWith("ytpl_") || id.startsWith("ytalbum_")) {
            val songs = streamingFallbackResolver.playlistSongs(id)
            val playlist = Playlist(
                id = id,
                name = songs.firstOrNull()?.album ?: "Music Playlist",
                description = "Discovered playlist",
                artworkUrl = songs.firstOrNull()?.albumArtUrl,
                songIds = songs.map { it.id },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isUserCreated = false
            )
            _state.value = PlaylistDetailsState(playlist, songs)
        } else {
            val playlist = libraryRepository.getPlaylistById(id)
            val knownSongs = (libraryRepository.getFavorites().first() + libraryRepository.getDownloadedSongs().first() + libraryRepository.getRecentlyPlayed().first()).distinctBy { it.id }.associateBy { it.id }
            val songs = playlist?.songIds.orEmpty().map { songId ->
                knownSongs[songId] ?: Song(songId, "Song $songId", "Unknown Artist", "Unknown Album", null, null, null, 0, null, null)
            }
            _state.value = PlaylistDetailsState(playlist, songs)
        }
    }
    fun rename(name: String) = viewModelScope.launch { _state.value.playlist?.let { libraryRepository.updatePlaylist(it.copy(name = name)); load(it.id) } }
    fun removeSong(songId: String) = viewModelScope.launch { _state.value.playlist?.let { libraryRepository.removeSongFromPlaylist(it.id, songId); load(it.id) } }
}

data class PlaylistDetailsState(val playlist: Playlist? = null, val songs: List<Song> = emptyList())

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(private val libraryRepository: LibraryRepository) : ViewModel() {
    val playlists = libraryRepository.getPlaylists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun createPlaylist(name: String) = viewModelScope.launch { libraryRepository.createPlaylist(name, null) }
    fun addSong(song: Song, playlistId: String) = viewModelScope.launch {
        libraryRepository.addSongToPlaylist(playlistId, song)
    }
}

@HiltViewModel
class DownloadStorageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val libraryRepository: LibraryRepository,
    private val downloadManager: DownloadManager,
    private val contentCacheRepository: ContentCacheRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DownloadStorageState())
    val state: StateFlow<DownloadStorageState> = _state.asStateFlow()
    val activeDownloads: StateFlow<Map<String, DownloadState>> = downloadManager.downloads
    fun refresh() = viewModelScope.launch {
        _state.value = DownloadStorageState(libraryRepository.getDownloadedSongs().first(), downloadManager.getStorageUsed(), cacheSize(context.cacheDir))
    }
    fun delete(song: Song) = viewModelScope.launch { downloadManager.deleteDownload(song); refresh() }
    fun cancel(songId: String) = downloadManager.cancelDownload(songId)
    fun retry(songId: String) = downloadManager.retryDownload(songId)
    fun clearAll() = viewModelScope.launch { _state.value.downloads.forEach { libraryRepository.deleteDownload(it.id) }; downloadManager.clearAllDownloads(); refresh() }
    fun clearMetadataCache() = viewModelScope.launch { contentCacheRepository.clearAll(); refresh() }
    fun clearRuntimeCaches() = viewModelScope.launch {
        listOf("image_cache", "playback_cache").forEach { name -> context.cacheDir.resolve(name).deleteRecursively() }
        refresh()
    }
    private fun cacheSize(file: java.io.File): Long = if (!file.exists()) 0L else if (file.isFile) file.length() else file.listFiles()?.sumOf { cacheSize(it) } ?: 0L
}

data class DownloadStorageState(val downloads: List<Song> = emptyList(), val storageUsed: Long = 0L, val cacheUsed: Long = 0L)

@Composable private fun BackButton(onNavigateBack: () -> Unit) { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back") } }

@Composable
private fun HeroHeader(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: @Composable (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(Color.White.copy(alpha = .18f)), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(56.dp), tint = Color.White) }
        Spacer(Modifier.height(16.dp)); Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold); Text(subtitle, color = Color.White.copy(alpha = .85f)); if (action != null) { Spacer(Modifier.height(16.dp)); action() }
    }
}

@Composable private fun SectionTitle(text: String) { Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) }

@Composable
private fun SongRow(song: Song, onClick: () -> Unit, onMore: (() -> Unit)? = null) {
    ListItem(modifier = Modifier.clickable(onClick = onClick), leadingContent = { Icon(Icons.Filled.MusicNote, null, tint = MaterialTheme.colorScheme.primary) }, headlineContent = { Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis) }, supportingContent = { Text(song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis) }, trailingContent = { IconButton(onClick = { onMore?.invoke() }) { Icon(Icons.Filled.MoreVert, "More") } })
}

@Composable private fun EmptyPanel(title: String, subtitle: String) { Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Column(Modifier.padding(20.dp)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
private fun CacheCard(cacheBytes: Long, onClearMetadata: () -> Unit, onClearRuntime: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text("App cache", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("${"%.1f".format(cacheBytes / 1024f / 1024f)} MB cached images, playback and metadata", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClearMetadata) { Text("Clear metadata") }
                OutlinedButton(onClick = onClearRuntime) { Text("Clear images/audio cache") }
            }
        }
    }
}

@Composable
private fun StorageCard(bytes: Long, count: Int) {
    val mb = bytes / 1024f / 1024f
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text("Storage used", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text("${"%.1f".format(mb)} MB", style = MaterialTheme.typography.headlineMedium); Text("$count downloaded songs", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

val PrivacyPolicyBody = """
Slow Music stores your library, playlists, downloads, preferences, and playback history locally on this device while local database mode is enabled.

Online music metadata may be requested from public catalog and lyrics services when you search or open detail pages. AdMob and Google Play Billing can be enabled for production builds.

Replace this draft with your hosted privacy policy before Play Store release.
""".trimIndent()

val TermsBody = """
Slow Music is provided as a music discovery and playback application. You are responsible for using content according to applicable licenses and regional rules.

Subscriptions, ads, casting, downloads, and music discovery features may vary by region.

Replace this draft with your official terms of service before release.
""".trimIndent()

fun notificationPermissionName(): String = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else "android.permission.NOTIFICATIONS"
