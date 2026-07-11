package com.slowmusic.app.presentation.screens.search

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
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
    onPlayNext: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onDownload: (Song) -> Unit = {},
    onShare: (Song) -> Unit = {},
    onNotifications: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val recentSelections by viewModel.recentSelections.collectAsState()
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
            onPlayNext = { onPlayNext(song); selectedSong = null },
            onAddToQueue = { onAddToQueue(song); selectedSong = null },
            onDownload = { onDownload(song); selectedSong = null },
            onShare = { onShare(song); selectedSong = null },
            onGoToArtist = { onArtistClick(song.artist); selectedSong = null },
            onGoToAlbum = { selectedSong = null }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                    scrolledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextField(
                            value = uiState.query,
                            onValueChange = viewModel::updateQuery,
                            placeholder = { Text("What do you want to play?") },
                            singleLine = true,
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.86f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.76f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.search(uiState.query); focusManager.clearFocus() }),
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
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
                        IconButton(
                            onClick = onNotifications,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f))
                        ) { Icon(Icons.Filled.Notifications, "Notifications", tint = MaterialTheme.colorScheme.onSurface) }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { viewModel.selectTab(SearchTab.ALL) }, label = { Text("All") })
                if (uiState.downloadedSongs.isNotEmpty()) AssistChip(onClick = { viewModel.selectTab(SearchTab.DOWNLOADS) }, label = { Text("Downloaded") })
                if (uiState.localSongs.isNotEmpty()) AssistChip(onClick = { viewModel.selectTab(SearchTab.LOCAL) }, label = { Text("Local") })
            }
            voiceMessage?.let { Text(it, modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.primary) }
            when {
                uiState.isSearching -> LoadingIndicator()
                uiState.query.isEmpty() -> BrowseContent(
                    genres = uiState.genres,
                    recentSelections = recentSelections,
                    searchHistory = searchHistory,
                    suggestions = uiState.suggestions,
                    onRecentSongClick = { song -> viewModel.showSelectedSong(song); onSongClick(song, recentSelections.ifEmpty { listOf(song) }) },
                    onRecentSongMore = { selectedSong = it },
                    onGenreClick = onGenreClick,
                    onHistoryItemClick = { viewModel.updateQuery(it); viewModel.search(it) },
                    onClearHistory = viewModel::clearSearchHistory
                )
                uiState.error != null -> ErrorMessage(uiState.error ?: "Search failed", onRetry = { viewModel.search(uiState.query) })
                else -> SearchResults(
                    state = uiState,
                    onTabSelected = viewModel::selectTab,
                    onSongClick = { song, queue -> viewModel.showSelectedSong(song); onSongClick(song, queue) },
                    onArtistClick = onArtistClick,
                    onAlbumClick = onAlbumClick,
                    onPlaylistClick = onPlaylistClick,
                    onMore = { selectedSong = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BrowseContent(
    genres: List<Genre>,
    recentSelections: List<Song>,
    searchHistory: List<String>,
    suggestions: List<String>,
    onRecentSongClick: (Song) -> Unit,
    onRecentSongMore: (Song) -> Unit,
    onGenreClick: (String) -> Unit,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
        if (suggestions.isNotEmpty()) {
            stickyHeader { LockedSectionHeader("Suggestions") }
            items(suggestions) { suggestion -> SearchTextRow(Icons.Filled.Lightbulb, suggestion) { onHistoryItemClick(suggestion) } }
        }
        if (recentSelections.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recently selected", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onClearHistory) { Text("Clear searches") }
                }
            }
            items(recentSelections.take(10)) { song ->
                SongListItem(
                    song = song,
                    onClick = { onRecentSongClick(song) },
                    onMoreClick = { onRecentSongMore(song) }
                )
            }
        } else if (searchHistory.isNotEmpty()) {
            stickyHeader { LockedSectionHeader("Suggestions from history") }
            items(searchHistory.take(4)) { query -> SearchTextRow(Icons.Filled.History, query) { onHistoryItemClick(query) } }
        }
        stickyHeader { LockedSectionHeader("Browse All") }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(genres) { genre -> GenreChip(name = genre.name, onClick = { onGenreClick(genre.id) }) }
            }
        }
    }
}

@Composable
private fun LockedSectionHeader(title: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.90f))
    ) {
        SectionHeader(title)
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
private fun TopResultCard(song: Song, onClick: () -> Unit, onMore: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = song.albumArtUrl,
                contentDescription = null,
                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Top result", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(song.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            IconButton(onClick = onMore) { Icon(Icons.Filled.MoreVert, null) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
        if (state.selectedTab == SearchTab.ALL && songsForTab.isNotEmpty()) {
            item { TopResultCard(song = songsForTab.first(), onClick = { onSongClick(songsForTab.first(), songsForTab) }, onMore = { onMore(songsForTab.first()) }) }
        }
        if (showSongs && songsForTab.isNotEmpty()) {
            stickyHeader { LockedSectionHeader(if (state.selectedTab == SearchTab.LOCAL) "Local Songs" else if (state.selectedTab == SearchTab.DOWNLOADS) "Downloads" else "Songs") }
            items(songsForTab) { song -> SongListItem(song, { onSongClick(song, songsForTab) }, { onMore(song) }) }
        }
        if (showPlaylists && results.playlists.isNotEmpty()) {
            stickyHeader { LockedSectionHeader("Playlists") }
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
            stickyHeader { LockedSectionHeader("Artists") }
            item { LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(results.artists) { artist -> ArtistCard(name = artist.name, imageUrl = artist.imageUrl, onClick = { onArtistClick(artist.id) }) } } }
        }
        if (showAlbums && results.albums.isNotEmpty()) {
            stickyHeader { LockedSectionHeader("Albums") }
            item { LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(results.albums) { album -> AlbumCard(title = album.title, artist = album.artist, artworkUrl = album.artworkUrl, onClick = { onAlbumClick(album.id) }) } } }
        }
        if ((showSongs && songsForTab.isEmpty()) && (!showArtists || results.artists.isEmpty()) && (!showAlbums || results.albums.isEmpty()) && (!showPlaylists || results.playlists.isEmpty())) {
            item { EmptyState(icon = { Icon(Icons.Filled.SearchOff, null, Modifier.size(64.dp)) }, title = "No results found", subtitle = "Try another search or switch tabs") }
        }
    }
}
