package com.slowmusic.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val recentlyPlayed: List<Song> = emptyList(),
    val trendingSongs: List<Song> = emptyList(),
    val topSongs: List<Song> = emptyList(),
    val recommendations: List<Song> = emptyList(),
    val newReleases: List<Album> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val followedArtists: List<Artist> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: com.slowmusic.app.domain.repository.MusicRepository,
    private val libraryRepository: LibraryRepository,
    private val getGenresUseCase: GetGenresUseCase,
    private val getTrendingSongsUseCase: GetTrendingSongsUseCase,
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        observeRecentlyPlayed()
    }
    
    private fun observeRecentlyPlayed() {
        viewModelScope.launch {
            getRecentlyPlayedUseCase().collect { songs ->
                _uiState.update { it.copy(recentlyPlayed = songs) }
            }
        }
    }
    
    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load all content in parallel
                launch { loadGenres() }
                launch { loadTrendingSongs() }
                launch { loadTopSongs() }
                launch { loadRecommendations() }
                launch { loadNewReleases() }
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = e.message ?: "Failed to load content") 
                }
            }
        }
    }
    
    private suspend fun loadGenres() {
        try {
            val genres = getGenresUseCase()
            _uiState.update { it.copy(genres = genres) }
        } catch (e: Exception) {
            // Handle error silently for individual sections
        }
    }
    
    private suspend fun loadTrendingSongs() {
        try {
            val songs = getTrendingSongsUseCase()
            _uiState.update { it.copy(trendingSongs = songs) }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    private suspend fun loadTopSongs() {
        try {
            val songs = musicRepository.getTopSongs()
            _uiState.update { it.copy(topSongs = songs) }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    private suspend fun loadRecommendations() {
        try {
            val songs = getRecommendationsUseCase()
            _uiState.update { it.copy(recommendations = songs) }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    private suspend fun loadNewReleases() {
        try {
            val albums = musicRepository.getNewReleases()
            _uiState.update { it.copy(newReleases = albums) }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    fun refresh() {
        loadContent()
    }
}
