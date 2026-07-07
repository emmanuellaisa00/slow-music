package com.slowmusic.app.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.slowmusic.app.audio.EqualizerManager
import com.slowmusic.app.presentation.MainActivity
import com.slowmusic.app.streaming.StreamHeaderCodec
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MusicPlaybackService : MediaSessionService() {

    @Inject
    lateinit var equalizerManager: EqualizerManager

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var playbackCache: SimpleCache? = null

    private val defaultUa = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Mobile Safari/537.36"

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(defaultUa)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(30_000)
            .setAllowCrossProtocolRedirects(true)

        val resolvingFactory = ResolvingDataSource.Factory(httpFactory) { spec: DataSpec ->
            val uri: Uri = spec.uri
            val headers = StreamHeaderCodec.decode(uri.fragment)
            if (headers.isNullOrEmpty()) {
                spec
            } else {
                val cleanUri = uri.buildUpon().fragment(null).build()
                val ua = headers[StreamHeaderCodec.userAgentKey()]
                val extraHeaders = headers.filterKeys { it != StreamHeaderCodec.userAgentKey() }
                val merged = LinkedHashMap(spec.httpRequestHeaders).apply {
                    putAll(extraHeaders)
                    if (!ua.isNullOrBlank()) put("User-Agent", ua)
                }
                spec.buildUpon()
                    .setUri(cleanUri)
                    .setHttpRequestHeaders(merged)
                    .build()
            }
        }

        val cacheDir = File(cacheDir, "playback_cache").also { it.mkdirs() }
        playbackCache = SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(200L * 1024L * 1024L), StandaloneDatabaseProvider(this))
        val dataSourceFactory = DefaultDataSource.Factory(
            this,
            CacheDataSource.Factory()
                .setCache(playbackCache!!)
                .setUpstreamDataSourceFactory(resolvingFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        )

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { exoPlayer ->
                exoPlayer.addListener(object : Player.Listener {
                    override fun onAudioSessionIdChanged(audioSessionId: Int) {
                        equalizerManager.attachToAudioSession(audioSessionId)
                    }
                })
                if (exoPlayer.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                    equalizerManager.attachToAudioSession(exoPlayer.audioSessionId)
                }
            }

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val sessionPlayer = mediaSession?.player
        if (sessionPlayer == null || !sessionPlayer.playWhenReady || sessionPlayer.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        player?.release()
        player = null
        equalizerManager.release()
        playbackCache?.release()
        playbackCache = null
        super.onDestroy()
    }
}
