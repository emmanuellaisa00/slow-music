package com.slowmusic.app.presentation.screens.search

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.components.*
import com.slowmusic.app.presentation.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSongClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onGenreClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val focusManager = LocalFocusManager.current
    
    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            spokenText?.let { viewModel.search(it) }
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadGenres()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.updateQuery(it) },
                        placeholder = { Text("Songs, artists, albums...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.search(uiState.query)
                                focusManager.clearFocus()
                            }
                        ),
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateQuery("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Voice Search Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                        }
                        voiceSearchLauncher.launch(intent)
                    },
                    label = { Text("Voice Search") },
                    leadingIcon = {
                        Icon(Icons.Filled.Mic, contentDescription = null)
                    }
                )
            }
            
            when {
                uiState.isSearching -> LoadingIndicator()
                uiState.query.isEmpty() -> BrowseContent(
                    genres = uiState.genres,
                    searchHistory = searchHistory,
                    onGenreClick = onGenreClick,
                    onHistoryItemClick = { 
                        viewModel.updateQuery(it)
                        viewModel.search(it)
                    },
                    onClearHistory = { viewModel.clearSearchHistory() }
                )
                uiState.error != null -> ErrorMessage(
                    message = uiState.error!!,
                    onRetry = { viewModel.search(uiState.query) }
                )
                else -> SearchResults(
                    results = uiState.results,
                    onSongClick = onSongClick,
                    onArtistClick = onArtistClick,
                    onAlbumClick = onAlbumClick
                )
            }
        }
    }
}

@Composable
private fun BrowseContent(
    genres: List<Genre>,
    searchHistory: List<String>,
    onGenreClick: (String) -> Unit,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Search History
        if (searchHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Searches",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onClearHistory) {
                        Text("Clear all")
                    }
                }
            }
            
            items(searchHistory.take(5)) { query ->
                ListItem(
                    headlineContent = { Text(query) },
                    leadingContent = {
                        Icon(Icons.Filled.History, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onHistoryItemClick(query) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Browse Genres
        item {
            Text(
                text = "Browse All",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres) { genre ->
                    GenreChip(
                        name = genre.name,
                        onClick = { onGenreClick(genre.id) }
                    )
                }
            }
        }
        
        // Category Sections
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Popular Categories",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        items(listOf("Pop", "Rock", "Hip-Hop", "Latin", "R&B", "Electronic", "Country", "Jazz")) { category ->
            ListItem(
                headlineContent = { Text(category) },
                leadingContent = {
                    Icon(Icons.Filled.MusicNote, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable { onHistoryItemClick(category) }
            )
        }
    }
}

@Composable
private fun SearchResults(
    results: SearchResult,
    onSongClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Songs
        if (results.songs.isNotEmpty()) {
            item {
                SectionHeader(title = "Songs")
            }
            
            items(results.songs.take(10)) { song ->
                SongListItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onMoreClick = { }
                )
            }
        }
        
        // Artists
        if (results.artists.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "Artists")
            }
            
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(results.artists) { artist ->
                        ArtistCard(
                            name = artist.name,
                            imageUrl = artist.imageUrl,
                            onClick = { onArtistClick(artist.id) }
                        )
                    }
                }
            }
        }
        
        // Albums
        if (results.albums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "Albums")
            }
            
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(results.albums) { album ->
                        AlbumCard(
                            title = album.title,
                            artist = album.artist,
                            artworkUrl = album.artworkUrl,
                            onClick = { onAlbumClick(album.id) }
                        )
                    }
                }
            }
        }
        
        // Empty state
        if (results.songs.isEmpty() && results.artists.isEmpty() && results.albums.isEmpty()) {
            item {
                EmptyState(
                    icon = {
                        Icon(
                            Icons.Filled.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                    },
                    title = "No results found",
                    subtitle = "Try searching for something else"
                )
            }
        }
    }
}
