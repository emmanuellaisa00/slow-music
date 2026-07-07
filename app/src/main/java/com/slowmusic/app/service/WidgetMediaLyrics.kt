package com.slowmusic.app.service

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.slowmusic.app.R
import com.slowmusic.app.domain.model.Song
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Music Widget Service - Handles widget updates and actions
 */
@AndroidEntryPoint
class MusicWidgetService : Service() {
    
    @Inject
    lateinit var okHttpClient: OkHttpClient
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE -> {
                val song = intent.getParcelableExtra<Song>(EXTRA_SONG)
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                song?.let { updateWidget(it, isPlaying) }
            }
            ACTION_PLAY_PAUSE -> sendBroadcast(Intent(ACTION_PLAY_PAUSE_BROADCAST))
            ACTION_NEXT -> sendBroadcast(Intent(ACTION_NEXT_BROADCAST))
            ACTION_PREVIOUS -> sendBroadcast(Intent(ACTION_PREVIOUS_BROADCAST))
        }
        return START_NOT_STICKY
    }
    
    private fun updateWidget(song: Song, isPlaying: Boolean) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, com.slowmusic.app.widget.MusicWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        serviceScope.launch {
            val views = createWidgetViews(song, isPlaying)
            appWidgetManager.updateAppWidget(appWidgetIds, views)
        }
    }
    
    private suspend fun createWidgetViews(song: Song, isPlaying: Boolean): RemoteViews {
        return RemoteViews(packageName, R.layout.widget_music).apply {
            setTextViewText(R.id.widget_song_title, song.title)
            setTextViewText(R.id.widget_artist_name, song.artist)
            
            // Update play/pause icon
            val playPauseIcon = if (isPlaying) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            }
            setImageViewResource(R.id.widget_play_button, playPauseIcon)
            
            // Load album art
            song.albumArtUrl?.let { url ->
                try {
                    val loader = ImageLoader(this@MusicWidgetService)
                    val request = ImageRequest.Builder(this@MusicWidgetService)
                        .data(url)
                        .allowHardware(false)
                        .build()
                    
                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        val bitmap = result.drawable.toBitmap()
                        setImageViewBitmap(R.id.widget_album_art, bitmap)
                    }
                } catch (e: Exception) {
                    // Use default icon
                }
            }
            
            // Click intents
            val mainIntent = Intent(this@MusicWidgetService, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                this@MusicWidgetService, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)
            
            val playPauseIntent = Intent(this@MusicWidgetService, MusicWidgetService::class.java).apply {
                action = ACTION_PLAY_PAUSE
            }
            val playPausePendingIntent = PendingIntent.getService(
                this@MusicWidgetService, 0, playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_play_button, playPausePendingIntent)
            
            val nextIntent = Intent(this@MusicWidgetService, MusicWidgetService::class.java).apply {
                action = ACTION_NEXT
            }
            val nextPendingIntent = PendingIntent.getService(
                this@MusicWidgetService, 1, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_next_button, nextPendingIntent)
            
            val prevIntent = Intent(this@MusicWidgetService, MusicWidgetService::class.java).apply {
                action = ACTION_PREVIOUS
            }
            val prevPendingIntent = PendingIntent.getService(
                this@MusicWidgetService, 2, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_previous_button, prevPendingIntent)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    companion object {
        const val ACTION_UPDATE = "com.slowmusic.app.widget.UPDATE"
        const val ACTION_PLAY_PAUSE = "com.slowmusic.app.widget.PLAY_PAUSE"
        const val ACTION_NEXT = "com.slowmusic.app.widget.NEXT"
        const val ACTION_PREVIOUS = "com.slowmusic.app.widget.PREVIOUS"
        
        const val ACTION_PLAY_PAUSE_BROADCAST = "com.slowmusic.app.PLAY_PAUSE"
        const val ACTION_NEXT_BROADCAST = "com.slowmusic.app.NEXT"
        const val ACTION_PREVIOUS_BROADCAST = "com.slowmusic.app.PREVIOUS"
        
        const val EXTRA_SONG = "extra_song"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        
        fun updateWidget(context: Context, song: Song?, isPlaying: Boolean) {
            val intent = Intent(context, MusicWidgetService::class.java).apply {
                action = ACTION_UPDATE
                song?.let { putExtra(EXTRA_SONG, it) }
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            context.startService(intent)
        }
    }
}

/**
 * Lyrics API Service - Fetches song lyrics
 */
package com.slowmusic.app.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface LyricsApiService {
    @GET("v1/")
    suspend fun getLyrics(
        @Query("artist") artist: String,
        @Query("title") title: String,
        @Query("duration") duration: Long? = null
    ): LyricsResponse
}

data class LyricsResponse(
    @SerializedName("lyrics") val lyrics: String?,
    @SerializedName("source") val source: String?
)

/**
 * LRCLib API for synced lyrics
 */
interface LrcLibApiService {
    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrcLibSearchResult>
    
    @GET("api/get")
    suspend fun getLyrics(
        @Query("id") id: Int
    ): LrcLibLyrics
}

data class LrcLibSearchResult(
    @SerializedName("id") val id: Int,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("albumName") val albumName?
)

data class LrcLibLyrics(
    @SerializedName("id") val id: Int,
    @SerializedName("lyrics") val lyrics: String?,
    @SerializedName("syncedLyrics") val syncedLyrics: String?
)

/**
 * Lyrics Repository Implementation
 */
package com.slowmusic.app.data.repository

import com.slowmusic.app.data.remote.api.LrcLibApiService
import com.slowmusic.app.data.remote.api.LyricsApiService
import com.slowmusic.app.domain.model.Lyrics
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.domain.repository.LyricsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val lyricsApiService: LyricsApiService,
    private val lrcLibApiService: LrcLibApiService
) : LyricsRepository {
    
    override suspend fun getLyrics(song: Song): Lyrics? {
        return try {
            // Try LRCLib first for synced lyrics
            val query = "${song.artist} ${song.title}"
            val searchResults = lrcLibApiService.searchLyrics(query)
            
            if (searchResults.isNotEmpty()) {
                val result = searchResults.first()
                val lyricsData = lrcLibApiService.getLyrics(result.id)
                
                return Lyrics(
                    songId = song.id,
                    text = lyricsData.syncedLyrics ?: lyricsData.lyrics ?: "",
                    source = "LRCLib"
                )
            }
            
            // Fallback to lyrics.ovh
            val response = lyricsApiService.getLyrics(song.artist, song.title, song.duration)
            
            if (response.lyrics != null) {
                Lyrics(
                    songId = song.id,
                    text = response.lyrics,
                    source = response.source
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Logger.e("LyricsRepository", "Failed to get lyrics: ${e.message}")
            null
        }
    }
}
