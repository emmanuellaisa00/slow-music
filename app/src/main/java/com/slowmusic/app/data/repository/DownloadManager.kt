package com.slowmusic.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.repository.PreferencesRepository
import com.slowmusic.app.streaming.StreamHeaderCodec
import com.slowmusic.app.streaming.StreamingFallbackResolver
import com.slowmusic.app.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.RandomAccessFile
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Download Manager - Handles downloading songs for offline playback
 */
@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val libraryRepository: LibraryRepository,
    private val streamingFallbackResolver: StreamingFallbackResolver,
    private val preferencesRepository: PreferencesRepository
) {
    private val _downloads = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadState>> = _downloads.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    suspend fun downloadSong(song: Song): Result<File> = withContext(Dispatchers.IO) {
        try {
            Logger.d("DownloadManager", "Starting download: ${song.title}")
            val prefs = preferencesRepository.getUserPreferences().first()
            if (prefs.downloadOnWifiOnly && !isOnWifi()) {
                _downloads.update { it + (song.id to DownloadState.Failed("Wi-Fi only is enabled")) }
                return@withContext Result.failure(Exception("Wi-Fi only is enabled"))
            }
            _downloads.update { it + (song.id to DownloadState.Downloading(0f)) }

            val resolved = resolveDownloadUrl(song)
                ?: return@withContext Result.failure(Exception("No downloadable stream found"))
            val (url, headers) = resolved

            val downloadsDir = File(context.filesDir, "downloads")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val extension = if (url.contains(".m4a", true)) "m4a" else if (url.contains(".webm", true)) "webm" else "mp4"
            val file = File(downloadsDir, "${song.id}_${sanitizeFileName(song.title)}.$extension")

            val probe = probe(url, headers)
            if (probe.acceptRanges && probe.length > 4L * 1024L * 1024L) {
                chunkedDownload(url, headers, file, probe.length, song.id)
            } else {
                serialDownload(url, headers, file, probe.length, song.id)
            }

            val downloadedSong = song.copy(isDownloaded = true, localPath = file.absolutePath, downloadProgress = 100)
            libraryRepository.downloadSong(downloadedSong)
            _downloads.update { it - song.id }
            Logger.d("DownloadManager", "Download complete: ${song.title}")
            Result.success(file)
        } catch (e: Exception) {
            Logger.e("DownloadManager", "Download error: ${e.message}", e)
            _downloads.update { it - song.id }
            Result.failure(e)
        }
    }

    private suspend fun resolveDownloadUrl(song: Song): Pair<String, Map<String, String>>? {
        song.localPath?.takeIf { it.isNotBlank() }?.let { return it to emptyMap() }
        val stream = streamingFallbackResolver.resolveSong(song) ?: return null
        val headers = LinkedHashMap<String, String>()
        if (stream.userAgent.isNotBlank()) headers["User-Agent"] = stream.userAgent
        headers.putAll(stream.headers)
        return stream.url to headers
    }

    private data class Probe(val length: Long, val acceptRanges: Boolean)

    private fun probe(url: String, headers: Map<String, String>): Probe {
        val req = Request.Builder().url(url).head().apply { headers.forEach { (k, v) -> header(k, v) } }.build()
        return runCatching {
            okHttpClient.newCall(req).execute().use { resp ->
                Probe(resp.header("Content-Length")?.toLongOrNull() ?: -1L, resp.header("Accept-Ranges")?.contains("bytes", true) == true)
            }
        }.getOrDefault(Probe(-1L, false))
    }

    private fun serialDownload(url: String, headers: Map<String, String>, file: File, contentLength: Long, songId: String) {
        val request = Request.Builder().url(url).apply { headers.forEach { (k, v) -> header(k, v) } }.build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Download failed: ${response.code}")
        val body = response.body ?: throw Exception("Empty response body")
        val length = if (contentLength > 0) contentLength else body.contentLength()
        var done = 0L
        FileOutputStream(file).use { output ->
            body.byteStream().use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    done += read
                    _downloads.update { it + (songId to DownloadState.Downloading(if (length > 0) done.toFloat() / length else 0f)) }
                }
            }
        }
    }

    private suspend fun chunkedDownload(url: String, headers: Map<String, String>, file: File, length: Long, songId: String) = coroutineScope {
        val chunks = 4
        RandomAccessFile(file, "rw").use { it.setLength(length) }
        val completed = java.util.concurrent.atomic.AtomicLong(0L)
        val ranges = (0 until chunks).map { index ->
            val start = (length * index) / chunks
            val end = if (index == chunks - 1) length - 1 else (length * (index + 1) / chunks) - 1
            start to end
        }
        ranges.map { (start, end) ->
            async(Dispatchers.IO) {
                val req = Request.Builder().url(url).header("Range", "bytes=$start-$end").apply { headers.forEach { (k, v) -> header(k, v) } }.build()
                okHttpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful && resp.code != 206) throw Exception("Chunk failed: ${resp.code}")
                    val body = resp.body ?: throw Exception("Empty chunk")
                    RandomAccessFile(file, "rw").use { raf ->
                        raf.seek(start)
                        body.byteStream().use { input ->
                            val buffer = ByteArray(64 * 1024)
                            while (true) {
                                val read = input.read(buffer)
                                if (read == -1) break
                                raf.write(buffer, 0, read)
                                val done = completed.addAndGet(read.toLong())
                                _downloads.update { it + (songId to DownloadState.Downloading(done.toFloat() / length)) }
                            }
                        }
                    }
                }
            }
        }.awaitAll()
    }

    fun cancelDownload(songId: String) {
        Logger.d("DownloadManager", "Cancelled download: $songId")
        _downloads.update { it - songId }
    }
    
    suspend fun deleteDownload(song: Song) = withContext(Dispatchers.IO) {
        song.localPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
                Logger.d("DownloadManager", "Deleted file: $path")
            }
        }
        libraryRepository.deleteDownload(song.id)
    }
    
    private fun isOnWifi(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun getDownloadedFiles(): List<File> {
        val downloadsDir = File(context.filesDir, "downloads")
        return downloadsDir.listFiles()?.toList() ?: emptyList()
    }
    
    fun getStorageUsed(): Long {
        return getDownloadedFiles().sumOf { it.length() }
    }
    
    suspend fun clearAllDownloads() = withContext(Dispatchers.IO) {
        getDownloadedFiles().forEach { it.delete() }
        Logger.d("DownloadManager", "Cleared all downloads")
    }
    
    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
    
    fun cleanup() {
        scope.cancel()
    }
}

sealed class DownloadState {
    data class Downloading(val progress: Float) : DownloadState()
    data class Completed(val file: File) : DownloadState()
    data class Failed(val error: String) : DownloadState()
}

/**
 * Share Manager - Handles sharing songs
 */
@Singleton
class ShareManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun shareSong(song: Song): String {
        val shareText = buildString {
            append("🎵 Check out \"${song.title}\" by ${song.artist}\n")
            append("🎧 Listen on Slow Music\n\n")
            append("https://music.apple.com/song/${song.id}")
        }
        return shareText
    }
    
    fun sharePlaylist(playlistName: String, songCount: Int): String {
        return buildString {
            append("🎶 Check out my \"$playlistName\" playlist!\n")
            append("🎵 $songCount songs\n\n")
            append("Listen on Slow Music")
        }
    }
    
    fun createShareIntent(text: String): android.content.Intent {
        return android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
    }
}

/**
 * Sleep Timer Manager
 */
@Singleton
class SleepTimerManager @Inject constructor() {
    private val _remainingTime = MutableStateFlow<Long?>(null)
    val remainingTime: StateFlow<Long?> = _remainingTime.asStateFlow()
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    private var timerJob: Job? = null
    private var endTime: Long = 0L
    
    suspend fun startTimer(minutes: Int, onFinish: () -> Unit) = withContext(Dispatchers.Main) {
        cancel()
        
        _isActive.value = true
        endTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (_isActive.value) {
                val remaining = endTime - System.currentTimeMillis()
                
                if (remaining <= 0) {
                    _isActive.value = false
                    _remainingTime.value = null
                    onFinish()
                    break
                }
                
                _remainingTime.value = remaining
                delay(1000)
            }
        }
        
        Logger.d("SleepTimer", "Timer started for $minutes minutes")
    }
    
    fun cancel() {
        timerJob?.cancel()
        timerJob = null
        _isActive.value = false
        _remainingTime.value = null
        Logger.d("SleepTimer", "Timer cancelled")
    }
    
    fun addTime(minutes: Int) {
        if (_isActive.value) {
            endTime += (minutes * 60 * 1000L)
            Logger.d("SleepTimer", "Added $minutes minutes")
        }
    }
    
    fun getFormattedRemainingTime(): String {
        val remaining = _remainingTime.value ?: return ""
        val minutes = (remaining / 1000 / 60).toInt()
        val seconds = ((remaining / 1000) % 60).toInt()
        return "%02d:%02d".format(minutes, seconds)
    }
}

/**
 * Audio Focus Manager
 */
@Singleton
class AudioFocusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
    
    private var focusChangeListener: android.media.AudioManager.OnAudioFocusChangeListener? = null
    private var hasAudioFocus = false
    
    fun requestAudioFocus(
        onFocusGained: () -> Unit,
        onFocusLost: () -> Unit,
        onFocusLostTransient: () -> Unit,
        onFocusLostDuck: () -> Unit
    ) {
        focusChangeListener = android.media.AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                android.media.AudioManager.AUDIOFOCUS_GAIN -> {
                    hasAudioFocus = true
                    onFocusGained()
                    Logger.d("AudioFocus", "Focus gained")
                }
                android.media.AudioManager.AUDIOFOCUS_LOSS -> {
                    hasAudioFocus = false
                    onFocusLost()
                    Logger.d("AudioFocus", "Focus lost")
                }
                android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    onFocusLostTransient()
                    Logger.d("AudioFocus", "Focus lost transient")
                }
                android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    onFocusLostDuck()
                    Logger.d("AudioFocus", "Focus lost duck")
                }
            }
        }
        
        val result = audioManager.requestAudioFocus(
            focusChangeListener,
            android.media.AudioManager.STREAM_MUSIC,
            android.media.AudioManager.AUDIOFOCUS_GAIN
        )
        
        hasAudioFocus = result == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Logger.d("AudioFocus", "Request result: $result")
    }
    
    fun abandonAudioFocus() {
        focusChangeListener?.let {
            audioManager.abandonAudioFocus(it)
            hasAudioFocus = false
            Logger.d("AudioFocus", "Focus abandoned")
        }
    }
    
    fun hasFocus(): Boolean = hasAudioFocus
}
