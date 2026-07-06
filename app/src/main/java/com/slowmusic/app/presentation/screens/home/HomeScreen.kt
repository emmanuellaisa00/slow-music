package com.slowmusic.app.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.presentation.components.*
import com.slowmusic.app.presentation.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onGenreClick: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSeeAll: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadContent()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Slow Music",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorMessage(
                message = uiState.error!!,
                onRetry = { viewModel.loadContent() }
            )
            else -> HomeContent(
                uiState = uiState,
                onSongClick = onSongClick,
                onArtistClick = onArtistClick,
                onAlbumClick = onAlbumClick,
                onGenreClick = onGenreClick,
                onSeeAllClick = onNavigateToSeeAll,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onSongClick: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onGenreClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Good Evening Section
        item {
            SectionHeader(title = "Good Evening")
        }
        
        // Quick Picks
        if (uiState.recentlyPlayed.isNotEmpty()) {
            item {
                QuickPicksRow(
                    songs = uiState.recentlyPlayed.take(6),
                    onSongClick = onSongClick
                )
            }
        }
        
        // Genres
        if (uiState.genres.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "Browse by Genre")
            }
            
            item {
                GenreRow(
                    genres = uiState.genres,
                    onGenreClick = onGenreClick
                )
            }
        }
        
        // Trending Songs
        if (uiState.trendingSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Trending Now",
                    onSeeAllClick = { onSeeAllClick("trending") }
                )
            }
            
            item {
                SongRow(
                    songs = uiState.trendingSongs,
                    onSongClick = onSongClick
                )
            }
        }
        
        // Top Songs
        if (uiState.topSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Top Hits",
                    onSeeAllClick = { onSeeAllClick("top") }
                )
            }
            
            item {
                SongRow(
                    songs = uiState.topSongs,
                    onSongClick = onSongClick
                )
            }
        }
        
        // New Releases
        if (uiState.newReleases.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "New Releases",
                    onSeeAllClick = { onSeeAllClick("new") }
                )
            }
            
            item {
                AlbumRow(
                    albums = uiState.newReleases,
                    onAlbumClick = onAlbumClick
                )
            }
        }
        
        // Recommended
        if (uiState.recommendations.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Recommended For You",
                    onSeeAllClick = { onSeeAllClick("recommended") }
                )
            }
            
            item {
                SongRow(
                    songs = uiState.recommendations,
                    onSongClick = onSongClick
                )
            }
        }
    }
}

@Composable
private fun QuickPicksRow(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(songs) { song ->
            QuickPickCard(
                song = song,
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
private fun QuickPickCard(
    song: Song,
    onClick: () -> Unit
) {
    com.slowmusic.app.presentation.components.SongCard(
        song = song,
        onClick = onClick,
        onMoreClick = { }
    )
}

@Composable
private fun GenreRow(
    genres: List<com.slowmusic.app.domain.model.Genre>,
    onGenreClick: (String) -> Unit
) {
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

@Composable
private fun SongRow(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(songs) { song ->
            SongCard(
                song = song,
                onClick = { onSongClick(song) },
                onMoreClick = { }
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
        contentPadding = PaddingValues(horizontal = 16.dp),
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
