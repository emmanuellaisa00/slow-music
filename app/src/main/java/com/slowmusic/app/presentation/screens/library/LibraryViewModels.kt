package com.slowmusic.app.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val getDownloadedSongsUseCase: GetDownloadedSongsUseCase,
    private val getFollowedArtistsUseCase: GetFollowedArtistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {
    
    val favorites: StateFlow<List<Song>> = getFavoritesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val playlists: StateFlow<List<Playlist>> = getPlaylistsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val downloadedSongs: StateFlow<List<Song>> = getDownloadedSongsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val followedArtists: StateFlow<List<Artist>> = getFollowedArtistsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun createPlaylist() {
        viewModelScope.launch {
            createPlaylistUseCase("New Playlist", null)
        }
    }
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {
    
    val favorites: StateFlow<List<Song>> = getFavoritesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun removeFromFavorites(song: Song) {
        viewModelScope.launch {
            toggleFavoriteUseCase(song)
        }
    }
}

@HiltViewModel
class RecentlyPlayedViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase
) : ViewModel() {
    
    val recentPlays: StateFlow<List<Song>> = getRecentlyPlayedUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun clearHistory() {
        viewModelScope.launch {
            libraryRepository.clearRecentlyPlayed()
        }
    }
}

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val getDownloadedSongsUseCase: GetDownloadedSongsUseCase
) : ViewModel() {
    
    val downloads: StateFlow<List<Song>> = getDownloadedSongsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun deleteDownload(song: Song) {
        viewModelScope.launch {
            libraryRepository.deleteDownload(song.id)
        }
    }
}
