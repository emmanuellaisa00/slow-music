package com.slowmusic.app.presentation

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.PlaybackException
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.slowmusic.app.data.local.QueueStateRepository
import com.slowmusic.app.data.local.SavedQueueState
import com.slowmusic.app.data.repository.DownloadManager
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.repository.LyricsRepository
import com.slowmusic.app.domain.repository.PreferencesRepository
import com.slowmusic.app.service.MusicPlaybackService
import com.slowmusic.app.service.MusicWidgetService
import com.slowmusic.app.streaming.StreamingFallbackResolver
import com.slowmusic.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private val lyricsRepository: LyricsRepository,
    private val downloadManager: DownloadManager,
    private val streamingFallbackResolver: StreamingFallbackResolver,
    private val queueStateRepository: QueueStateRepository
) : AndroidViewModel(application) {

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.DARK)

    val navigationStyle: StateFlow<NavigationStyle> = preferencesRepository.getNavigationStyle()
        .stateIn(viewModelScope, SharingStarted.Eagerly, NavigationStyle.TABS)

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.getUserPreferences()
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

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

    private val _lyrics = MutableStateFlow<String?>(null)
    val lyrics: StateFlow<String?> = _lyrics.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var progressJob: Job? = null
    private var playRequestId: Long = 0L
    private var lyricsRequestId: Long = 0L

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            syncPlaybackState()
            if (playbackState == Player.STATE_ENDED) handleQueueEnded()
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
                        queueStateRepository.recordPlay(song)
                        loadLyrics(song)
                        persistQueueState()
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
        observeRuntimePreferences()
        restoreQueueState()
    }

    private fun observeRuntimePreferences() {
        viewModelScope.launch {
            userPreferences.collect { prefs ->
                streamingFallbackResolver.setBackendUrl(prefs.resolverBackendUrl)
                mediaController?.playbackParameters = PlaybackParameters(prefs.playbackSpeed.coerceIn(0.5f, 2f))
            }
        }
    }

    private fun restoreQueueState() {
        viewModelScope.launch {
            val saved = queueStateRepository.restore() ?: return@launch
            _queue.value = saved.queue
            _currentIndex.value = saved.currentIndex
            _currentSong.value = saved.queue.getOrNull(saved.currentIndex) ?: saved.queue.firstOrNull()
            _repeatMode.value = saved.repeatMode
            _isShuffled.value = saved.shuffleEnabled
            _progress.value = if ((_currentSong.value?.duration ?: 0L) > 0) {
                (saved.positionMs.toFloat() / (_currentSong.value?.duration ?: 1L)).coerceIn(0f, 1f)
            } else 0f
            _playbackState.value = PlaybackState.PAUSED
        }
    }

    private fun persistQueueState() {
        val queue = _queue.value
        if (queue.isEmpty()) return
        viewModelScope.launch {
            queueStateRepository.save(
                SavedQueueState(
                    queue = queue,
                    currentSongId = _currentSong.value?.id,
                    currentIndex = _currentIndex.value.coerceIn(0, queue.lastIndex),
                    positionMs = mediaController?.currentPosition ?: ((_currentSong.value?.duration ?: 0L) * _progress.value).toLong(),
                    repeatMode = _repeatMode.value,
                    shuffleEnabled = _isShuffled.value
                )
            )
        }
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
                controller.playbackParameters = PlaybackParameters(userPreferences.value.playbackSpeed.coerceIn(0.5f, 2f))
                            controller.playbackParameters = PlaybackParameters(userPreferences.value.playbackSpeed.coerceIn(0.5f, 2f))
                            streamingFallbackResolver.setBackendUrl(userPreferences.value.resolverBackendUrl)
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
        val requestId = ++playRequestId
        val playableQueue = queue.ifEmpty { listOf(song) }
        val clickedIndex = playableQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        // Stop current audio immediately so the UI never waits for fallback resolution
        // while the previous track continues playing.
        mediaController?.run {
            stop()
            clearMediaItems()
        }
        stopProgressTicker()
        _currentSong.value = song
        _queue.value = playableQueue
        _currentIndex.value = clickedIndex
        _progress.value = 0f
        _lyrics.value = null
        _playbackState.value = PlaybackState.BUFFERING
        updateWidget()

        viewModelScope.launch {
            val resolvedSong = resolveForPlayback(song)
            if (requestId != playRequestId) return@launch

            _currentSong.value = resolvedSong
            val controller = mediaController
            val firstItem = toMediaItem(resolvedSong)

            if (controller != null && firstItem != null) {
                controller.setMediaItem(firstItem, 0L)
                controller.prepare()
                controller.play()
                controller.shuffleModeEnabled = _isShuffled.value
                controller.repeatMode = media3RepeatMode(_repeatMode.value)
                controller.playbackParameters = PlaybackParameters(userPreferences.value.playbackSpeed.coerceIn(0.5f, 2f))
            } else {
                Logger.w("Player", "No playable URL for ${song.title}; using local UI playback state")
                fallbackStartPlayback(resolvedSong, playableQueue)
            }

            libraryRepository.addToRecentlyPlayed(resolvedSong)
            libraryRepository.incrementPlayCount(resolvedSong.id)
            queueStateRepository.recordPlay(resolvedSong)
            loadLyrics(resolvedSong)
            persistQueueState()
            updateWidget()
            enrichQueueWithSmartRadio(resolvedSong, requestId)

            // Resolve the rest of the queue after playback starts. This keeps first
            // audio start fast while still preparing next/previous items.
            viewModelScope.launch {
                val resolvedQueue = playableQueue.toMutableList()
                resolvedQueue[clickedIndex.coerceAtMost(resolvedQueue.lastIndex)] = resolvedSong
                for ((index, candidate) in playableQueue.withIndex()) {
                    if (requestId != playRequestId) return@launch
                    if (index == clickedIndex) continue
                    val resolved = resolveForPlayback(candidate)
                    resolvedQueue[index] = resolved
                    toMediaItem(resolved)?.let { mediaController?.addMediaItem(it) }
                    _queue.value = resolvedQueue.toList()
                }
            }
        }
    }

    fun playNext() {
        val controller = mediaController
        if (controller != null && controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
            controller.play()
            persistQueueState()
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
            persistQueueState()
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
        persistQueueState()
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        mediaController?.repeatMode = media3RepeatMode(_repeatMode.value)
        persistQueueState()
    }

    fun playNextInQueue(song: Song) {
        val insertAt = (_currentIndex.value + 1).coerceAtLeast(0)
        val current = _queue.value.toMutableList()
        val existing = current.indexOfFirst { it.id == song.id || (it.title.equals(song.title, true) && it.artist.equals(song.artist, true)) }
        if (existing >= 0) current.removeAt(existing)
        val target = insertAt.coerceAtMost(current.size)
        current.add(target, song)
        _queue.value = current
        viewModelScope.launch {
            val resolved = resolveForPlayback(song)
            val mediaItem = toMediaItem(resolved)
            if (mediaItem != null) {
                val mediaIndex = target.coerceAtMost(mediaController?.mediaItemCount ?: 0)
                mediaController?.addMediaItem(mediaIndex, mediaItem)
            }
            _queue.value = _queue.value.map { if (it.id == song.id) resolved else it }
            persistQueueState()
        }
    }

    fun addToQueue(song: Song) {
        _queue.value = _queue.value + song
        viewModelScope.launch {
            val resolved = resolveForPlayback(song)
            _queue.value = _queue.value.map { if (it.id == song.id) resolved else it }
            toMediaItem(resolved)?.let { mediaController?.addMediaItem(it) }
            persistQueueState()
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (from !in currentQueue.indices || to !in currentQueue.indices || from == to) return
        val item = currentQueue.removeAt(from)
        currentQueue.add(to, item)
        _queue.value = currentQueue
        mediaController?.let { controller ->
            if (from < controller.mediaItemCount && to < controller.mediaItemCount) controller.moveMediaItem(from, to)
        }
        if (_currentIndex.value == from) _currentIndex.value = to
        persistQueueState()
    }

    fun saveQueueAsPlaylist() {
        val items = _queue.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            val playlist = libraryRepository.createPlaylist("Queue ${System.currentTimeMillis()}", "Saved from now playing queue")
            items.forEach { libraryRepository.addSongToPlaylist(playlist.id, it) }
        }
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
        if (currentQueue.isEmpty()) clearQueue() else persistQueueState()
    }

    fun clearQueue() {
        mediaController?.clearMediaItems()
        _queue.value = emptyList()
        _currentIndex.value = 0
        _currentSong.value = null
        _progress.value = 0f
        _playbackState.value = PlaybackState.IDLE
        stopProgressTicker()
        viewModelScope.launch { queueStateRepository.clear() }
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
        if (!song.localPath.isNullOrBlank()) return song
        val resolved = streamingFallbackResolver.resolveSongWithMetadata(song) ?: return song.copy(previewUrl = null)
        return resolved.song.copy(
            streamUrl = streamingFallbackResolver.encodeStreamUrl(resolved.stream),
            previewUrl = null,
            isDownloaded = song.isDownloaded,
            localPath = song.localPath
        )
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

    private fun loadLyrics(song: Song) {
        val requestId = ++lyricsRequestId
        _lyrics.value = null
        viewModelScope.launch {
            val text = runCatching { lyricsRepository.getLyrics(song)?.text }.getOrNull()
            if (requestId == lyricsRequestId && _currentSong.value?.id == song.id) {
                _lyrics.value = text
            }
        }
    }

    private fun enrichQueueWithSmartRadio(seed: Song, requestId: Long) {
        if (!userPreferences.value.autoPlaySimilar) return
        viewModelScope.launch {
            val radio = buildSmartRadio(seed).take(10)
            if (requestId != playRequestId || radio.isEmpty()) return@launch
            val current = _queue.value.toMutableList()
            val insertAt = (_currentIndex.value + 1).coerceIn(0, current.size)
            val existingKeys = current.map { it.title.lowercase().trim() to it.artist.lowercase().trim() }.toMutableSet()
            val newSongs = radio.filter { existingKeys.add(it.title.lowercase().trim() to it.artist.lowercase().trim()) }
            if (newSongs.isEmpty()) return@launch
            current.addAll(insertAt, newSongs)
            _queue.value = current

            // Resolve the first few upcoming songs first so rapid "Next" taps have
            // playable items ready. Resolve the rest afterwards to avoid delaying UI.
            val priority = newSongs.take(4)
            val rest = newSongs.drop(4)
            priority.mapIndexed { offset, candidate ->
                async {
                    val resolved = resolveForPlayback(candidate)
                    insertAt + offset to resolved
                }
            }.awaitAll().forEach { (index, resolved) ->
                if (requestId != playRequestId) return@launch
                _queue.value = _queue.value.toMutableList().also { if (index in it.indices) it[index] = resolved }
                toMediaItem(resolved)?.let { mediaController?.addMediaItem(index.coerceAtMost(mediaController?.mediaItemCount ?: 0), it) }
            }
            for ((offset, candidate) in rest.withIndex()) {
                if (requestId != playRequestId) return@launch
                val resolved = resolveForPlayback(candidate)
                val index = insertAt + priority.size + offset
                _queue.value = _queue.value.toMutableList().also { if (index in it.indices) it[index] = resolved }
                toMediaItem(resolved)?.let { mediaController?.addMediaItem(index.coerceAtMost(mediaController?.mediaItemCount ?: 0), it) }
            }
            persistQueueState()
        }
    }

    private suspend fun buildSmartRadio(seed: Song): List<Song> {
        val seeds = buildList {
            add("${seed.artist} radio")
            add("${seed.artist} ${seed.genre ?: "songs"}")
            add("${seed.title} ${seed.artist} similar")
            seed.album.takeIf { it.isNotBlank() }?.let { add("$it ${seed.artist}") }
            seed.genre?.takeIf { it.isNotBlank() }?.let { add("$it ${seed.artist} mix") }
            _queue.value.takeLast(8).forEach { other ->
                if (!other.artist.equals(seed.artist, true)) add("${seed.artist} ${other.artist} collaboration")
            }
        }.distinct()
        return coroutineScope {
            seeds.flatMap { base ->
                listOf(
                    base,
                    "$base official audio",
                    "$base topic",
                    "$base similar artists",
                    "$base radio mix"
                )
            }
                .distinct()
                .map { query -> async { streamingFallbackResolver.searchSongs(query, 6) } }
                .awaitAll()
                .flatten()
                .distinctBy { it.title.lowercase().trim() to it.artist.lowercase().trim() }
                .filterNot { it.title.equals(seed.title, true) && it.artist.equals(seed.artist, true) }
        }
    }

    private fun handleQueueEnded() {
        if (!userPreferences.value.autoPlaySimilar) return
        val seed = _currentSong.value ?: return
        viewModelScope.launch {
            val seeds = buildList {
                add("${seed.artist} ${seed.genre ?: "songs"}")
                add("${seed.artist} radio")
                add("${seed.title} ${seed.artist}")
                seed.genre?.takeIf { it.isNotBlank() }?.let { add("$it ${seed.artist}") }
                _queue.value.takeLast(8).forEach { queued ->
                    if (!queued.artist.equals(seed.artist, true)) add("${seed.artist} ${queued.artist} collab similar")
                }
            }.distinct()
            val similar = seeds.flatMap { query -> streamingFallbackResolver.searchSongs(query, 8) }
                .distinctBy { it.title.lowercase() to it.artist.lowercase() }
                .filterNot { candidate ->
                    _queue.value.any { it.id == candidate.id || (it.title.equals(candidate.title, true) && it.artist.equals(candidate.artist, true)) }
                }
                .take(12)
            if (similar.isNotEmpty()) {
                val expanded = _queue.value + similar
                _queue.value = expanded
                for (candidate in similar) {
                    toMediaItem(resolveForPlayback(candidate))?.let { mediaController?.addMediaItem(it) }
                }
                playNext()
            }
        }
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
