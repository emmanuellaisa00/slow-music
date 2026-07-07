package com.slowmusic.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.data.repository.ContentCacheRepository
import com.slowmusic.app.data.repository.HomeContentSnapshot
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val loadedFromCache: Boolean = false,
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
    private val contentCacheRepository: ContentCacheRepository,
    private val getGenresUseCase: GetGenresUseCase,
    private val getTrendingSongsUseCase: GetTrendingSongsUseCase,
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var hasLoaded = false

    init { observeRecentlyPlayed() }

    private fun observeRecentlyPlayed() {
        viewModelScope.launch {
            getRecentlyPlayedUseCase().collect { songs -> _uiState.update { it.copy(recentlyPlayed = songs) } }
        }
    }

    fun loadContent(forceRefresh: Boolean = false) {
        if (hasLoaded && !forceRefresh) return
        hasLoaded = true
        viewModelScope.launch {
            val cached = if (!forceRefresh) contentCacheRepository.getHome() else null
            if (cached != null) {
                applySnapshot(cached, fromCache = true)
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = null) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = !hasVisibleContent(), isRefreshing = hasVisibleContent(), error = null) }
            try {
                val genres = async { runCatching { getGenresUseCase() }.getOrDefault(emptyList()) }
                val trending = async { runCatching { getTrendingSongsUseCase() }.getOrDefault(emptyList()) }
                val top = async { runCatching { musicRepository.getTopSongs() }.getOrDefault(emptyList()) }
                val recs = async { runCatching { getRecommendationsUseCase() }.getOrDefault(emptyList()) }
                val releases = async { runCatching { musicRepository.getNewReleases() }.getOrDefault(emptyList()) }
                val snapshot = HomeContentSnapshot(
                    trendingSongs = trending.await(),
                    topSongs = top.await(),
                    recommendations = recs.await(),
                    newReleases = releases.await(),
                    genres = genres.await()
                )
                contentCacheRepository.saveHome(snapshot)
                applySnapshot(snapshot, fromCache = false)
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Failed to load content") }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { contentCacheRepository.clearHome() }
        loadContent(forceRefresh = true)
    }

    private fun applySnapshot(snapshot: HomeContentSnapshot, fromCache: Boolean) {
        _uiState.update {
            it.copy(
                trendingSongs = snapshot.trendingSongs,
                topSongs = snapshot.topSongs,
                recommendations = snapshot.recommendations,
                newReleases = snapshot.newReleases,
                genres = snapshot.genres,
                loadedFromCache = fromCache
            )
        }
    }

    private fun hasVisibleContent(): Boolean = _uiState.value.run {
        trendingSongs.isNotEmpty() || topSongs.isNotEmpty() || recommendations.isNotEmpty() || newReleases.isNotEmpty() || genres.isNotEmpty()
    }
}
