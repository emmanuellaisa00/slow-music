package com.slowmusic.app.presentation.screens.search

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit = {},
    onGenreClick: (String) -> Unit,
    onAddToPlaylist: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onDownload: (Song) -> Unit = {},
    onShare: (Song) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val focusManager = LocalFocusManager.current
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var voiceMessage by remember { mutableStateOf<String?>(null) }

    val recordAudioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        voiceMessage = if (granted) "Tap Voice Search again to speak" else "Microphone permission denied"
    }
    val voiceSearchLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { viewModel.search(it) }
        }
    }

    LaunchedEffect(Unit) { viewModel.loadGenres() }

    selectedSong?.let { song ->
        SongOptionsBottomSheet(
            song = song,
            onDismiss = { selectedSong = null },
            onAddToPlaylist = { onAddToPlaylist(song); selectedSong = null },
            onAddToQueue = { onAddToQueue(song); selectedSong = null },
            onDownload = { onDownload(song); selectedSong = null },
            onShare = { onShare(song); selectedSong = null },
            onGoToArtist = { onArtistClick(song.artist); selectedSong = null },
            onGoToAlbum = { selectedSong = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 0.dp),
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::updateQuery,
                        placeholder = { Text("Songs, artists, albums, playlists...") },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.search(uiState.query); focusManager.clearFocus() }),
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.query.isNotEmpty()) IconButton(onClick = { viewModel.updateQuery("") }) { Icon(Icons.Filled.Clear, "Clear") }
                                IconButton(onClick = {
                                    recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    }
                                    runCatching { voiceSearchLauncher.launch(intent) }.onFailure { voiceMessage = "Voice search is not available on this device" }
                                }) { Icon(Icons.Filled.Mic, "Voice search") }
                            }
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {
                        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        }
                        runCatching { voiceSearchLauncher.launch(intent) }.onFailure { voiceMessage = "Voice search is not available on this device" }
                    },
                    label = { Text("Voice Search") },
                    leadingIcon = { Icon(Icons.Filled.Mic, null) }
                )
                if (uiState.downloadedSongs.isNotEmpty()) AssistChip(onClick = { viewModel.selectTab(SearchTab.DOWNLOADS) }, label = { Text("Downloaded") })
                if (uiState.localSongs.isNotEmpty()) AssistChip(onClick = { viewModel.selectTab(SearchTab.LOCAL) }, label = { Text("Local") })
            }
            voiceMessage?.let { Text(it, modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.primary) }
            when {
                uiState.isSearching -> LoadingIndicator()
                uiState.query.isEmpty() -> BrowseContent(uiState.genres, searchHistory, uiState.suggestions, onGenreClick, { viewModel.updateQuery(it); viewModel.search(it) }, viewModel::clearSearchHistory)
                uiState.error != null -> ErrorMessage(uiState.error ?: "Search failed", onRetry = { viewModel.search(uiState.query) })
                else -> SearchResults(
                    state = uiState,
                    onTabSelected = viewModel::selectTab,
                    onSongClick = onSongClick,
                    onArtistClick = onArtistClick,
                    onAlbumClick = onAlbumClick,
                    onPlaylistClick = onPlaylistClick,
                    onMore = { selectedSong = it }
                )
            }
        }
    }
}

@Composable
private fun BrowseContent(
    genres: List<Genre>,
    searchHistory: List<String>,
    suggestions: List<String>,
    onGenreClick: (String) -> Unit,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
        if (suggestions.isNotEmpty()) {
            item { SectionHeader("Suggestions") }
            items(suggestions) { suggestion -> SearchTextRow(Icons.Filled.Lightbulb, suggestion) { onHistoryItemClick(suggestion) } }
        }
        if (searchHistory.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent Searches", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onClearHistory) { Text("Clear all") }
                }
            }
            items(searchHistory.take(8)) { query -> SearchTextRow(Icons.Filled.History, query) { onHistoryItemClick(query) } }
        }
        item { SectionHeader("Browse All") }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(genres) { genre -> GenreChip(name = genre.name, onClick = { onGenreClick(genre.id) }) }
            }
        }
        item { SectionHeader("Popular Categories") }
        items(listOf("Pop", "Rock", "Hip-Hop", "Latin", "R&B", "Electronic", "Country", "Jazz", "Afrobeats", "Gospel")) { category ->
            SearchTextRow(Icons.Filled.MusicNote, category) { onHistoryItemClick(category) }
        }
    }
}

@Composable
private fun SearchTextRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Icon(icon, null) },
        headlineContent = { Text(text) },
        trailingContent = { Icon(Icons.Filled.KeyboardArrowRight, null) }
    )
}

@Composable
private fun SearchResults(
    state: SearchUiState,
    onTabSelected: (SearchTab) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onMore: (Song) -> Unit
) {
    val results = state.results
    val songsForTab = when (state.selectedTab) {
        SearchTab.LOCAL -> state.localSongs
        SearchTab.DOWNLOADS -> state.downloadedSongs
        else -> results.songs
    }
    val showSongs = state.selectedTab in listOf(SearchTab.ALL, SearchTab.SONGS, SearchTab.LOCAL, SearchTab.DOWNLOADS)
    val showAlbums = state.selectedTab in listOf(SearchTab.ALL, SearchTab.ALBUMS)
    val showArtists = state.selectedTab in listOf(SearchTab.ALL, SearchTab.ARTISTS)
    val showPlaylists = state.selectedTab in listOf(SearchTab.ALL, SearchTab.PLAYLISTS)

    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            ScrollableTabRow(selectedTabIndex = state.selectedTab.ordinal, edgePadding = 8.dp) {
                SearchTab.values().forEach { tab ->
                    Tab(selected = state.selectedTab == tab, onClick = { onTabSelected(tab) }, text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }
        }
        if (showSongs && songsForTab.isNotEmpty()) {
            item { SectionHeader(if (state.selectedTab == SearchTab.LOCAL) "Local Songs" else if (state.selectedTab == SearchTab.DOWNLOADS) "Downloads" else "Songs") }
            items(songsForTab) { song -> SongListItem(song, { onSongClick(song, songsForTab) }, { onMore(song) }) }
        }
        if (showPlaylists && results.playlists.isNotEmpty()) {
            item { SectionHeader("Playlists") }
            items(results.playlists) { playlist ->
                ListItem(
                    modifier = Modifier.clickable { onPlaylistClick(playlist.id) },
                    leadingContent = { Icon(Icons.Filled.QueueMusic, null, tint = MaterialTheme.colorScheme.primary) },
                    headlineContent = { Text(playlist.name) },
                    supportingContent = { Text("${playlist.songIds.size} songs") },
                    trailingContent = { Icon(Icons.Filled.KeyboardArrowRight, null) }
                )
            }
        }
        if (showArtists && results.artists.isNotEmpty()) {
            item { SectionHeader("Artists") }
            item { LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(results.artists) { artist -> ArtistCard(name = artist.name, imageUrl = artist.imageUrl, onClick = { onArtistClick(artist.id) }) } } }
        }
        if (showAlbums && results.albums.isNotEmpty()) {
            item { SectionHeader("Albums") }
            item { LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(results.albums) { album -> AlbumCard(title = album.title, artist = album.artist, artworkUrl = album.artworkUrl, onClick = { onAlbumClick(album.id) }) } } }
        }
        if ((showSongs && songsForTab.isEmpty()) && (!showArtists || results.artists.isEmpty()) && (!showAlbums || results.albums.isEmpty()) && (!showPlaylists || results.playlists.isEmpty())) {
            item { EmptyState(icon = { Icon(Icons.Filled.SearchOff, null, Modifier.size(64.dp)) }, title = "No results found", subtitle = "Try another search or switch tabs") }
        }
    }
}
