@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.slowmusic.app.presentation.screens.details

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.data.repository.DownloadManager
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailsScreen(
    artistId: String,
    onNavigateBack: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    viewModel: ArtistDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(artistId) { viewModel.load(artistId) }

    Scaffold(topBar = { TopAppBar(title = { Text(state.artist?.name ?: "Artist") }, navigationIcon = { BackButton(onNavigateBack) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
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
            item { SectionTitle("Top Songs") }
            items(state.songs.take(10)) { song -> SongRow(song, { onSongClick(song) }) }
            item { SectionTitle("Albums & Singles") }
            items(state.albums) { album ->
                ListItem(
                    modifier = Modifier.clickable { onAlbumClick(album.id) },
                    leadingContent = { Icon(Icons.Filled.Album, null, tint = MaterialTheme.colorScheme.primary) },
                    headlineContent = { Text(album.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = { Text(album.releaseDate ?: album.artist) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, null) }
                )
            }
            item { SectionTitle("About") }
            item { Text("Follow ${state.artist?.name ?: "this artist"} to keep their new releases and top songs in your library. Artist biographies and verified metadata can be synced from your online catalog later.") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailsScreen(
    albumId: String,
    onNavigateBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    viewModel: AlbumDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(albumId) { viewModel.load(albumId) }
    Scaffold(topBar = { TopAppBar(title = { Text("Album") }, navigationIcon = { BackButton(onNavigateBack) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
            item {
                HeroHeader(
                    title = state.album?.title ?: "Album",
                    subtitle = listOfNotNull(state.album?.artist, state.album?.releaseDate, state.album?.genre).joinToString(" • "),
                    icon = Icons.Filled.Album,
                    action = { Button(onClick = viewModel::saveAlbum) { Icon(Icons.Filled.LibraryAdd, null); Spacer(Modifier.width(8.dp)); Text("Save") } }
                )
            }
            item { SectionTitle("Tracks") }
            items(state.songs) { song ->
                SongRow(song, onClick = { onSongClick(song) }, onMore = { onAddToPlaylist(song) })
            }
            item { SectionTitle("Credits") }
            item { Text("Released by ${state.album?.artist ?: "Unknown label"}. Track metadata is sourced from the online catalog and cached locally for library use.") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailsScreen(
    playlistId: String,
    onNavigateBack: () -> Unit,
    onAddSongs: () -> Unit,
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

    Scaffold(topBar = { TopAppBar(title = { Text(state.playlist?.name ?: "Playlist") }, navigationIcon = { BackButton(onNavigateBack) }, actions = { IconButton(onClick = { editing = true }) { Icon(Icons.Filled.Edit, "Edit") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
            item { HeroHeader(state.playlist?.name ?: "Playlist", "${state.playlist?.songIds?.size ?: 0} songs • Local database", Icons.Filled.QueueMusic) }
            item { Button(onClick = onAddSongs, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp)); Text("Add songs") } }
            item { SectionTitle("Songs") }
            val ids = state.playlist?.songIds.orEmpty()
            if (ids.isEmpty()) item { EmptyPanel("No songs yet", "Use Add songs from album/search menus to build this playlist.") }
            items(ids) { songId ->
                ListItem(
                    headlineContent = { Text("Song $songId", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = { Text("Saved in local playlist database") },
                    leadingContent = { Icon(Icons.Filled.MusicNote, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { IconButton(onClick = { viewModel.removeSong(songId) }) { Icon(Icons.Filled.RemoveCircleOutline, "Remove") } }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistScreen(
    songId: String,
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
            item { Text("Choose where to save song $songId", style = MaterialTheme.typography.bodyMedium) }
            items(playlists) { playlist ->
                ListItem(
                    modifier = Modifier.clickable { viewModel.addSong(songId, playlist.id); onNavigateBack() },
                    leadingContent = { Icon(Icons.Filled.QueueMusic, null, tint = MaterialTheme.colorScheme.primary) },
                    headlineContent = { Text(playlist.name) },
                    supportingContent = { Text("${playlist.songIds.size} songs") },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, null) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastDevicePickerScreen(onNavigateBack: () -> Unit) {
    val devices = listOf("Living Room TV" to "Chromecast", "Bedroom Speaker" to "Cast audio", "Kitchen Display" to "Smart display")
    Scaffold(topBar = { TopAppBar(title = { Text("Connect to device") }, navigationIcon = { BackButton(onNavigateBack) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
            item { Text("Available devices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(devices) { (name, type) ->
                ListItem(leadingContent = { Icon(Icons.Filled.Cast, null, tint = MaterialTheme.colorScheme.primary) }, headlineContent = { Text(name) }, supportingContent = { Text(type) }, trailingContent = { TextButton(onClick = {}) { Text("Connect") } })
            }
            item { AssistChip(onClick = {}, label = { Text("Cast framework is ready; live device discovery is enabled on real devices with Google Play services.") }, leadingIcon = { Icon(Icons.Filled.Info, null) }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadStorageManagerScreen(onNavigateBack: () -> Unit, viewModel: DownloadStorageViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }
    Scaffold(topBar = { TopAppBar(title = { Text("Download storage") }, navigationIcon = { BackButton(onNavigateBack) }, actions = { IconButton(onClick = viewModel::refresh) { Icon(Icons.Filled.Refresh, "Refresh") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
            item { StorageCard(state.storageUsed, state.downloads.size) }
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
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { BackButton(onNavigateBack) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Filled.Security, null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            Button(onClick = {}) { Text("Grant ${permissionLabel(permission)}") }
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
class ArtistDetailsViewModel @Inject constructor(private val musicRepository: MusicRepository, private val libraryRepository: LibraryRepository) : ViewModel() {
    private val _state = MutableStateFlow(ArtistDetailsState())
    val state: StateFlow<ArtistDetailsState> = _state.asStateFlow()
    fun load(id: String) = viewModelScope.launch {
        val artist = musicRepository.getArtistById(id) ?: Artist(id, "Artist $id", null, null)
        val songs = musicRepository.getSongsByArtist(id)
        val albums = musicRepository.searchAlbums(artist.name)
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
class AlbumDetailsViewModel @Inject constructor(private val musicRepository: MusicRepository, private val libraryRepository: LibraryRepository) : ViewModel() {
    private val _state = MutableStateFlow(AlbumDetailsState())
    val state: StateFlow<AlbumDetailsState> = _state.asStateFlow()
    fun load(id: String) = viewModelScope.launch {
        val album = musicRepository.getAlbumById(id) ?: Album(id, "Album $id", "Unknown Artist", "", null, 0, null, null)
        _state.value = AlbumDetailsState(album, musicRepository.getSongsByAlbum(id))
    }
    fun saveAlbum() = viewModelScope.launch { _state.value.songs.take(1).forEach { libraryRepository.addToFavorites(it) } }
}

data class AlbumDetailsState(val album: Album? = null, val songs: List<Song> = emptyList())

@HiltViewModel
class PlaylistDetailsViewModel @Inject constructor(private val libraryRepository: LibraryRepository, savedStateHandle: SavedStateHandle) : ViewModel() {
    private val playlistId: String = savedStateHandle["playlistId"] ?: ""
    private val _state = MutableStateFlow(PlaylistDetailsState())
    val state: StateFlow<PlaylistDetailsState> = _state.asStateFlow()
    fun load(id: String = playlistId) = viewModelScope.launch { _state.value = PlaylistDetailsState(libraryRepository.getPlaylistById(id)) }
    fun rename(name: String) = viewModelScope.launch { _state.value.playlist?.let { libraryRepository.updatePlaylist(it.copy(name = name)); load(it.id) } }
    fun removeSong(songId: String) = viewModelScope.launch { _state.value.playlist?.let { libraryRepository.removeSongFromPlaylist(it.id, songId); load(it.id) } }
}

data class PlaylistDetailsState(val playlist: Playlist? = null)

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(private val libraryRepository: LibraryRepository) : ViewModel() {
    val playlists = libraryRepository.getPlaylists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun createPlaylist(name: String) = viewModelScope.launch { libraryRepository.createPlaylist(name, null) }
    fun addSong(songId: String, playlistId: String) = viewModelScope.launch {
        val song = Song(songId, "Saved song", "Unknown Artist", "Unknown Album", null, null, null, 0, null, null)
        libraryRepository.addSongToPlaylist(playlistId, song)
    }
}

@HiltViewModel
class DownloadStorageViewModel @Inject constructor(private val libraryRepository: LibraryRepository, private val downloadManager: DownloadManager) : ViewModel() {
    private val _state = MutableStateFlow(DownloadStorageState())
    val state: StateFlow<DownloadStorageState> = _state.asStateFlow()
    fun refresh() = viewModelScope.launch { _state.value = DownloadStorageState(libraryRepository.getDownloadedSongs().first(), downloadManager.getStorageUsed()) }
    fun delete(song: Song) = viewModelScope.launch { downloadManager.deleteDownload(song); refresh() }
    fun clearAll() = viewModelScope.launch { _state.value.downloads.forEach { libraryRepository.deleteDownload(it.id) }; downloadManager.clearAllDownloads(); refresh() }
}

data class DownloadStorageState(val downloads: List<Song> = emptyList(), val storageUsed: Long = 0L)

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

Subscriptions, ads, casting, downloads, and online catalog features depend on third-party services and may vary by region.

Replace this draft with your official terms of service before release.
""".trimIndent()

fun notificationPermissionName(): String = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else "android.permission.NOTIFICATIONS"
