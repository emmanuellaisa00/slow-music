package com.slowmusic.app.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.repository.PreferencesRepository
import com.slowmusic.app.data.repository.DownloadManager
import com.slowmusic.app.service.MusicWidgetService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val preferencesRepository: PreferencesRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadManager: DownloadManager
) : AndroidViewModel(application) {

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.DARK)

    val navigationStyle: StateFlow<NavigationStyle> = preferencesRepository.getNavigationStyle()
        .stateIn(viewModelScope, SharingStarted.Eagerly, NavigationStyle.TABS)

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)

    private val _isShuffled = MutableStateFlow(false)
    val isShuffled: StateFlow<Boolean> = _isShuffled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private var progressJob: Job? = null

    fun togglePlayPause() {
        when (_playbackState.value) {
            PlaybackState.PLAYING -> {
                _playbackState.value = PlaybackState.PAUSED
                stopProgressTicker()
            }
            PlaybackState.PAUSED, PlaybackState.IDLE, PlaybackState.ERROR -> {
                if (_currentSong.value == null && _queue.value.isNotEmpty()) {
                    playSong(_queue.value[_currentIndex.value.coerceIn(_queue.value.indices)])
                } else {
                    _playbackState.value = PlaybackState.PLAYING
                    startProgressTicker()
                }
            }
            PlaybackState.BUFFERING -> Unit
        }
        updateWidget()
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        _currentSong.value = song
        _queue.value = queue.ifEmpty { listOf(song) }
        _currentIndex.value = _queue.value.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        _progress.value = 0f
        _playbackState.value = PlaybackState.BUFFERING
        viewModelScope.launch {
            libraryRepository.addToRecentlyPlayed(song)
            libraryRepository.incrementPlayCount(song.id)
            delay(250)
            _playbackState.value = PlaybackState.PLAYING
            startProgressTicker()
            updateWidget()
        }
    }

    fun playNext() {
        val currentQueue = _queue.value
        if (currentQueue.isEmpty()) return
        val nextIndex = when (_repeatMode.value) {
            RepeatMode.ONE -> _currentIndex.value
            RepeatMode.ALL -> (_currentIndex.value + 1) % currentQueue.size
            RepeatMode.OFF -> if (_currentIndex.value < currentQueue.size - 1) _currentIndex.value + 1 else return
        }
        playSong(currentQueue[nextIndex], currentQueue)
    }

    fun playPrevious() {
        val currentQueue = _queue.value
        if (currentQueue.isEmpty()) return
        val prevIndex = when (_repeatMode.value) {
            RepeatMode.ONE -> _currentIndex.value
            RepeatMode.ALL -> if (_currentIndex.value > 0) _currentIndex.value - 1 else currentQueue.size - 1
            RepeatMode.OFF -> (_currentIndex.value - 1).coerceAtLeast(0)
        }
        playSong(currentQueue[prevIndex], currentQueue)
    }

    fun toggleShuffle() {
        val current = _queue.value
        _isShuffled.value = !_isShuffled.value
        if (_isShuffled.value && current.size > 1) {
            val active = _currentSong.value
            _queue.value = current.shuffled().let { shuffled ->
                if (active != null) listOf(active) + shuffled.filterNot { it.id == active.id } else shuffled
            }
            _currentIndex.value = 0
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
        if (index !in currentQueue.indices) return
        currentQueue.removeAt(index)
        _queue.value = currentQueue
        if (index < _currentIndex.value) _currentIndex.value--
        if (currentQueue.isEmpty()) clearQueue()
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _currentIndex.value = 0
        _currentSong.value = null
        _progress.value = 0f
        _playbackState.value = PlaybackState.IDLE
        stopProgressTicker()
        updateWidget()
    }

    fun seekTo(progress: Float) {
        _progress.value = progress.coerceIn(0f, 1f)
    }

    fun downloadSong(song: Song) {
        viewModelScope.launch { downloadManager.downloadSong(song) }
    }

    fun toggleFavorite(song: Song? = _currentSong.value) {
        song ?: return
        viewModelScope.launch {
            if (libraryRepository.isFavorite(song.id)) libraryRepository.removeFromFavorites(song.id) else libraryRepository.addToFavorites(song)
        }
    }

    fun setPlaybackState(state: PlaybackState) {
        _playbackState.value = state
        if (state == PlaybackState.PLAYING) startProgressTicker() else stopProgressTicker()
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (_playbackState.value == PlaybackState.PLAYING) {
                val song = _currentSong.value
                val duration = song?.duration ?: 0L
                val step = if (duration > 0) 1000f / duration else 0.003f
                val next = (_progress.value + step).coerceAtMost(1f)
                _progress.value = next
                if (next >= 1f) playNext()
                delay(1000)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateWidget() {
        MusicWidgetService.updateWidget(getApplication(), _currentSong.value, _playbackState.value == PlaybackState.PLAYING)
    }
}
