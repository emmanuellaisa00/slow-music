package com.slowmusic.app.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.data.repository.ContentCacheRepository
import com.slowmusic.app.data.repository.SearchContentSnapshot
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.repository.LocalMusicRepository
import com.slowmusic.app.domain.repository.MusicRepository
import com.slowmusic.app.domain.repository.PreferencesRepository
import com.slowmusic.app.domain.usecase.AddToSearchHistoryUseCase
import com.slowmusic.app.domain.usecase.GetSearchHistoryUseCase
import com.slowmusic.app.streaming.StreamingFallbackResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchTab { ALL, SONGS, ALBUMS, ARTISTS, PLAYLISTS, LOCAL, DOWNLOADS }

data class SearchUiState(
    val query: String = "",
    val selectedTab: SearchTab = SearchTab.ALL,
    val isSearching: Boolean = false,
    val error: String? = null,
    val results: SearchResult = SearchResult(emptyList(), emptyList(), emptyList(), emptyList()),
    val localSongs: List<Song> = emptyList(),
    val downloadedSongs: List<Song> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val genres: List<Genre> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val localMusicRepository: LocalMusicRepository,
    private val libraryRepository: LibraryRepository,
    private val preferencesRepository: PreferencesRepository,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val addToSearchHistoryUseCase: AddToSearchHistoryUseCase,
    private val streamingFallbackResolver: StreamingFallbackResolver,
    private val contentCacheRepository: ContentCacheRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val searchHistory: StateFlow<List<String>> = getSearchHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSelections: StateFlow<List<Song>> = libraryRepository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                query = query,
                suggestions = buildSuggestions(query, searchHistory.value)
            )
        }
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(300)
                search(query)
            }
        }
    }

    fun selectTab(tab: SearchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun showSelectedSong(song: Song) {
        _uiState.update { it.copy(query = "${song.title} • ${song.artist}", suggestions = emptyList()) }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null, query = query) }
            try {
                addToSearchHistoryUseCase(query)
                contentCacheRepository.getSearch(query)?.let { cached ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            results = cached.results,
                            localSongs = cached.localSongs,
                            downloadedSongs = cached.downloadedSongs,
                            suggestions = emptyList()
                        )
                    }
                    return@launch
                }

                val onlineSongs = async { runCatching { musicRepository.searchSongs(query) }.getOrDefault(emptyList()) }
                val fallbackSongs = async { runCatching { streamingFallbackResolver.searchSongs(query) }.getOrDefault(emptyList()) }
                val artists = async { runCatching { musicRepository.searchArtists(query) }.getOrDefault(emptyList()) }
                val albums = async { runCatching { musicRepository.searchAlbums(query) }.getOrDefault(emptyList()) }
                val fallbackAlbums = async { runCatching { streamingFallbackResolver.searchAlbums(query) }.getOrDefault(emptyList()) }
                val localSongs = async { runCatching { localMusicRepository.getLocalSongs().filter { it.matches(query) } }.getOrDefault(emptyList()) }
                val downloads = async { runCatching { libraryRepository.getDownloadedSongs().first().filter { it.matches(query) } }.getOrDefault(emptyList()) }
                val playlists = async { runCatching { libraryRepository.getPlaylists().first().filter { it.name.contains(query, true) || it.description?.contains(query, true) == true } }.getOrDefault(emptyList()) }
                val fallbackPlaylists = async { runCatching { streamingFallbackResolver.searchPlaylists(query) }.getOrDefault(emptyList()) }

                val downloaded = downloads.await()
                val locals = localSongs.await()
                val online = onlineSongs.await() + fallbackSongs.await()
                val mergedSongs = mergePreferLocal(downloaded + locals, online)
                val artistResults = artists.await()
                val albumResults = (albums.await() + fallbackAlbums.await()).distinctBy { album -> album.id }
                val playlistResults = (playlists.await() + fallbackPlaylists.await()).distinctBy { playlist -> playlist.id }

                val finalResults = SearchResult(
                    songs = mergedSongs,
                    artists = artistResults,
                    albums = albumResults,
                    playlists = playlistResults
                )
                contentCacheRepository.saveSearch(query, SearchContentSnapshot(finalResults, locals, downloaded))
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        results = finalResults,
                        localSongs = locals,
                        downloadedSongs = downloaded,
                        suggestions = emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, error = e.message ?: "Search failed") }
            }
        }
    }

    fun loadGenres() {
        viewModelScope.launch {
            runCatching { musicRepository.getGenres() }
                .onSuccess { genres -> _uiState.update { it.copy(genres = genres) } }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { preferencesRepository.clearSearchHistory() }
    }

    fun searchByGenre(genreId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null, selectedTab = SearchTab.SONGS) }
            try {
                val songs = musicRepository.getSongsByGenre(genreId)
                _uiState.update { it.copy(isSearching = false, results = SearchResult(songs, emptyList(), emptyList(), emptyList())) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, error = e.message ?: "Search failed") }
            }
        }
    }

    private fun buildSuggestions(query: String, history: List<String>): List<String> {
        if (query.length < 2) return emptyList()
        val fromHistory = history.filter { it.contains(query, true) }
        val common = listOf("Top hits", "New releases", "Afrobeats", "Gospel", "Hip-Hop", "R&B", "Love songs")
            .filter { it.contains(query, true) }
        return (fromHistory + common).distinct().take(6)
    }

    private fun Song.matches(query: String): Boolean =
        title.contains(query, true) || artist.contains(query, true) || album.contains(query, true) || genre?.contains(query, true) == true

    private fun mergePreferLocal(preferred: List<Song>, online: List<Song>): List<Song> {
        val result = preferred.toMutableList()
        online.forEach { candidate ->
            if (result.none { it.sameMusicalWork(candidate) }) result.add(candidate)
        }
        return result
    }

    private fun Song.sameMusicalWork(other: Song): Boolean {
        if (id == other.id) return true
        val durationClose = duration <= 0 || other.duration <= 0 || kotlin.math.abs(duration - other.duration) <= 3000
        return title.equals(other.title, true) && artist.equals(other.artist, true) && durationClose
    }
}
