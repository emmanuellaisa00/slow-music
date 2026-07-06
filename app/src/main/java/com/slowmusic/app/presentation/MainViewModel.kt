package com.slowmusic.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    // Theme
    val themeMode: StateFlow<ThemeMode> = preferencesRepository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.DARK)
    
    // Navigation Style
    val navigationStyle: StateFlow<NavigationStyle> = preferencesRepository.getNavigationStyle()
        .stateIn(viewModelScope, SharingStarted.Eagerly, NavigationStyle.TABS)
    
    // Playback State
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    // Current Song
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    // Queue
    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()
    
    private val _currentIndex = MutableStateFlow(0)
    
    // Shuffle & Repeat
    private val _isShuffled = MutableStateFlow(false)
    val isShuffled: StateFlow<Boolean> = _isShuffled.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    fun togglePlayPause() {
        _playbackState.value = when (_playbackState.value) {
            PlaybackState.PLAYING -> PlaybackState.PAUSED
            PlaybackState.PAUSED -> PlaybackState.PLAYING
            else -> PlaybackState.PLAYING
        }
    }
    
    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        _currentSong.value = song
        _queue.value = queue
        _currentIndex.value = queue.indexOf(song).coerceAtLeast(0)
        _playbackState.value = PlaybackState.BUFFERING
        
        viewModelScope.launch {
            // Simulate buffering
            kotlinx.coroutines.delay(500)
            _playbackState.value = PlaybackState.PLAYING
        }
    }
    
    fun playNext() {
        val currentQueue = _queue.value
        if (currentQueue.isEmpty()) return
        
        val nextIndex = when (_repeatMode.value) {
            RepeatMode.ONE -> _currentIndex.value
            RepeatMode.ALL -> (_currentIndex.value + 1) % currentQueue.size
            RepeatMode.OFF -> {
                if (_currentIndex.value < currentQueue.size - 1) {
                    _currentIndex.value + 1
                } else {
                    return
                }
            }
        }
        
        _currentIndex.value = nextIndex
        _currentSong.value = currentQueue[nextIndex]
        _playbackState.value = PlaybackState.BUFFERING
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _playbackState.value = PlaybackState.PLAYING
        }
    }
    
    fun playPrevious() {
        val currentQueue = _queue.value
        if (currentQueue.isEmpty()) return
        
        val prevIndex = when (_repeatMode.value) {
            RepeatMode.ONE -> _currentIndex.value
            RepeatMode.ALL -> if (_currentIndex.value > 0) _currentIndex.value - 1 else currentQueue.size - 1
            RepeatMode.OFF -> (_currentIndex.value - 1).coerceAtLeast(0)
        }
        
        _currentIndex.value = prevIndex
        _currentSong.value = currentQueue[prevIndex]
        _playbackState.value = PlaybackState.BUFFERING
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _playbackState.value = PlaybackState.PLAYING
        }
    }
    
    fun toggleShuffle() {
        _isShuffled.value = !_isShuffled.value
        if (_isShuffled.value) {
            _queue.value = _queue.value.shuffled()
        }
    }
    
    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }
    
    fun addToQueue(song: Song) {
        _queue.value = _queue.value + song
    }
    
    fun removeFromQueue(index: Int) {
        val currentQueue = _queue.value.toMutableList()
        currentQueue.removeAt(index)
        _queue.value = currentQueue
        
        if (index < _currentIndex.value) {
            _currentIndex.value--
        }
    }
    
    fun clearQueue() {
        _queue.value = emptyList()
        _currentIndex.value = 0
        _currentSong.value = null
        _playbackState.value = PlaybackState.IDLE
    }
    
    fun seekTo(position: Long) {
        // Would be handled by ExoPlayer
    }
    
    fun setPlaybackState(state: PlaybackState) {
        _playbackState.value = state
    }
}
