package com.slowmusic.app.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.MusicRepository
import com.slowmusic.app.domain.repository.PreferencesRepository
import com.slowmusic.app.domain.usecase.AddToSearchHistoryUseCase
import com.slowmusic.app.domain.usecase.GetSearchHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val error: String? = null,
    val results: SearchResult = SearchResult(emptyList(), emptyList(), emptyList(), emptyList()),
    val genres: List<Genre> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val preferencesRepository: PreferencesRepository,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val addToSearchHistoryUseCase: AddToSearchHistoryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    val searchHistory: StateFlow<List<String>> = getSearchHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private var searchJob: Job? = null
    
    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        
        // Debounce search
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(300)
                search(query)
            }
        }
    }
    
    fun search(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            
            try {
                addToSearchHistoryUseCase(query)
                
                val results = musicRepository.search(query)
                _uiState.update { it.copy(isSearching = false, results = results) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSearching = false, error = e.message ?: "Search failed") 
                }
            }
        }
    }
    
    fun loadGenres() {
        viewModelScope.launch {
            try {
                val genres = musicRepository.getGenres()
                _uiState.update { it.copy(genres = genres) }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun clearSearchHistory() {
        viewModelScope.launch {
            preferencesRepository.clearSearchHistory()
        }
    }
    
    fun searchByGenre(genreId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            
            try {
                val songs = musicRepository.getSongsByGenre(genreId)
                _uiState.update { 
                    it.copy(
                        isSearching = false, 
                        results = SearchResult(songs, emptyList(), emptyList(), emptyList())
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSearching = false, error = e.message ?: "Search failed") 
                }
            }
        }
    }
}
