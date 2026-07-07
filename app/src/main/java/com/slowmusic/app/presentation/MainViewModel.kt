package com.slowmusic.app.presentation

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.slowmusic.app.data.repository.DownloadManager
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.repository.PreferencesRepository
import com.slowmusic.app.service.MusicPlaybackService
import com.slowmusic.app.service.MusicWidgetService
import com.slowmusic.app.streaming.StreamingFallbackResolver
import com.slowmusic.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val preferencesRepository: PreferencesRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadManager: DownloadManager,
    private val streamingFallbackResolver: StreamingFallbackResolver
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

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var progressJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            syncPlaybackState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncPlaybackState()
            if (isPlaying) startProgressTicker() else stopProgressTicker()
            updateWidget()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val mediaId = mediaItem?.mediaId
            val index = _queue.value.indexOfFirst { it.id == mediaId }
            if (index >= 0) {
                _currentIndex.value = index
                _currentSong.value = _queue.value[index]
                viewModelScope.launch {
                    _currentSong.value?.let { song ->
                        libraryRepository.addToRecentlyPlayed(song)
                        libraryRepository.incrementPlayCount(song.id)
                    }
                }
            }
            updateProgressFromController()
            updateWidget()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _isShuffled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                else -> RepeatMode.OFF
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val song = _currentSong.value ?: return
            val position = mediaController?.currentPosition ?: 0L
            Logger.w("Player", "Playback error ${error.errorCode}; trying fallback re-resolve for ${song.title}")
            viewModelScope.launch {
                streamingFallbackResolver.invalidate(song)
                val resolved = streamingFallbackResolver.resolveSong(song)
                val controller = mediaController
                if (resolved != null && controller != null) {
                    val recovered = song.copy(streamUrl = streamingFallbackResolver.encodeStreamUrl(resolved))
                    _currentSong.value = recovered
                    val mediaItem = toMediaItem(recovered)
                    if (mediaItem != null) {
                        controller.setMediaItem(mediaItem, position)
                        controller.prepare()
                        controller.play()
                    }
                } else {
                    _playbackState.value = PlaybackState.ERROR
                }
            }
        }
    }

    init {
        connectMediaController()
    }

    private fun connectMediaController() {
        val app = getApplication<Application>()
        val token = SessionToken(app, ComponentName(app, MusicPlaybackService::class.java))
        controllerFuture = MediaController.Builder(app, token).buildAsync().also { future ->
            future.addListener(
                {
                    runCatching {
                        mediaController = future.get().also { controller ->
                            controller.addListener(playerListener)
                            controller.shuffleModeEnabled = _isShuffled.value
                            controller.repeatMode = media3RepeatMode(_repeatMode.value)
                            syncPlaybackState()
                            updateProgressFromController()
                        }
                    }.onFailure { Logger.e("Player", "Failed to connect MediaController", it) }
                },
                ContextCompat.getMainExecutor(app)
            )
        }
    }

    fun togglePlayPause() {
        val controller = mediaController
        if (controller == null) {
            fallbackTogglePlayPause()
            return
        }

        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.mediaItemCount == 0) {
                _currentSong.value?.let { playSong(it, _queue.value.ifEmpty { listOf(it) }) }
            } else {
                controller.play()
            }
        }
        syncPlaybackState()
        updateWidget()
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        val playableQueue = queue.ifEmpty { listOf(song) }
        _currentSong.value = song
        _queue.value = playableQueue
        _currentIndex.value = playableQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        _progress.value = 0f
        _playbackState.value = PlaybackState.BUFFERING

        viewModelScope.launch {
            val resolvedQueue = playableQueue.map { resolveForPlayback(it) }
            val resolvedSong = resolvedQueue.firstOrNull { it.id == song.id } ?: resolveForPlayback(song)
            _currentSong.value = resolvedSong
            _queue.value = resolvedQueue
            val items = resolvedQueue.mapNotNull(::toMediaItem)
            val startIndex = resolvedQueue.indexOfFirst { it.id == resolvedSong.id }.coerceAtLeast(0)
            val controller = mediaController

            if (controller != null && items.isNotEmpty()) {
                controller.setMediaItems(items, startIndex.coerceAtMost(items.lastIndex), 0L)
                controller.prepare()
                controller.play()
                controller.shuffleModeEnabled = _isShuffled.value
                controller.repeatMode = media3RepeatMode(_repeatMode.value)
            } else {
                Logger.w("Player", "No playable URL for ${song.title}; using local UI playback state")
                fallbackStartPlayback(resolvedSong, resolvedQueue)
            }

            libraryRepository.addToRecentlyPlayed(resolvedSong)
            libraryRepository.incrementPlayCount(resolvedSong.id)
            updateWidget()
        }
    }

    fun playNext() {
        val controller = mediaController
        if (controller != null && controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
            controller.play()
            return
        }

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
        val controller = mediaController
        if (controller != null && controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
            controller.play()
            return
        }

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
        _isShuffled.value = !_isShuffled.value
        mediaController?.shuffleModeEnabled = _isShuffled.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        mediaController?.repeatMode = media3RepeatMode(_repeatMode.value)
    }

    fun addToQueue(song: Song) {
        _queue.value = _queue.value + song
        toMediaItem(song)?.let { mediaController?.addMediaItem(it) }
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (index !in currentQueue.indices) return
        currentQueue.removeAt(index)
        _queue.value = currentQueue
        mediaController?.let { controller ->
            if (index < controller.mediaItemCount) controller.removeMediaItem(index)
        }
        if (index < _currentIndex.value) _currentIndex.value--
        if (currentQueue.isEmpty()) clearQueue()
    }

    fun clearQueue() {
        mediaController?.clearMediaItems()
        _queue.value = emptyList()
        _currentIndex.value = 0
        _currentSong.value = null
        _progress.value = 0f
        _playbackState.value = PlaybackState.IDLE
        stopProgressTicker()
        updateWidget()
    }

    fun seekTo(progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        _progress.value = clamped
        val controller = mediaController ?: return
        val duration = controller.duration.takeIf { it > 0 } ?: _currentSong.value?.duration ?: 0L
        if (duration > 0) controller.seekTo((duration * clamped).toLong())
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

    private suspend fun resolveForPlayback(song: Song): Song {
        val hasDirect = !song.localPath.isNullOrBlank() || !song.streamUrl.isNullOrBlank() || !song.previewUrl.isNullOrBlank()
        val needsFallback = song.id.startsWith("yt_") || !hasDirect
        if (!needsFallback) return song
        val stream = streamingFallbackResolver.resolveSong(song) ?: return song
        return song.copy(streamUrl = streamingFallbackResolver.encodeStreamUrl(stream))
    }

    private fun toMediaItem(song: Song): MediaItem? {
        val uri = song.localPath?.takeIf { it.isNotBlank() }
            ?: song.streamUrl?.takeIf { it.isNotBlank() }
            ?: song.previewUrl?.takeIf { it.isNotBlank() }
            ?: return null

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUrl?.let(Uri::parse))
            .build()

        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(Uri.parse(uri))
            .setMediaMetadata(metadata)
            .build()
    }

    private fun syncPlaybackState() {
        val controller = mediaController
        _playbackState.value = when {
            controller == null -> _playbackState.value
            controller.playbackState == Player.STATE_BUFFERING -> PlaybackState.BUFFERING
            controller.playbackState == Player.STATE_ENDED -> PlaybackState.IDLE
            controller.playbackState == Player.STATE_IDLE -> if (_currentSong.value == null) PlaybackState.IDLE else PlaybackState.PAUSED
            controller.isPlaying -> PlaybackState.PLAYING
            else -> PlaybackState.PAUSED
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (_playbackState.value == PlaybackState.PLAYING || mediaController?.isPlaying == true) {
                updateProgressFromController()
                delay(500)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
        updateProgressFromController()
    }

    private fun updateProgressFromController() {
        val controller = mediaController
        if (controller != null) {
            val duration = controller.duration
            _progress.value = if (duration > 0) {
                (controller.currentPosition.toFloat() / duration).coerceIn(0f, 1f)
            } else {
                _progress.value
            }
        } else {
            val songDuration = _currentSong.value?.duration ?: 0L
            if (songDuration > 0 && _playbackState.value == PlaybackState.PLAYING) {
                _progress.value = (_progress.value + 500f / songDuration).coerceAtMost(1f)
            }
        }
    }

    private fun media3RepeatMode(mode: RepeatMode): Int = when (mode) {
        RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        RepeatMode.OFF -> Player.REPEAT_MODE_OFF
    }

    private fun fallbackTogglePlayPause() {
        when (_playbackState.value) {
            PlaybackState.PLAYING -> {
                _playbackState.value = PlaybackState.PAUSED
                stopProgressTicker()
            }
            PlaybackState.PAUSED, PlaybackState.IDLE, PlaybackState.ERROR -> {
                _playbackState.value = PlaybackState.PLAYING
                startProgressTicker()
            }
            PlaybackState.BUFFERING -> Unit
        }
        updateWidget()
    }

    private fun fallbackStartPlayback(song: Song, queue: List<Song>) {
        _currentSong.value = song
        _queue.value = queue
        _currentIndex.value = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        _playbackState.value = PlaybackState.PLAYING
        startProgressTicker()
    }

    private fun updateWidget() {
        MusicWidgetService.updateWidget(getApplication(), _currentSong.value, _playbackState.value == PlaybackState.PLAYING)
    }

    override fun onCleared() {
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        progressJob?.cancel()
        super.onCleared()
    }
}
