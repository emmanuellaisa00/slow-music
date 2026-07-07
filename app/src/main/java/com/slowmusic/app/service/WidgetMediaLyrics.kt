package com.slowmusic.app.service

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.slowmusic.app.R
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.presentation.MainActivity
import com.slowmusic.app.widget.MusicWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Music Widget Service - Handles widget updates and actions.
 *
 * The original file mixed three packages in one Kotlin file, which prevents the
 * project from compiling. Lyrics APIs now live in data/remote/api and this file
 * only owns the widget service.
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
                val song = intent.parcelable<Song>(EXTRA_SONG)
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                song?.let { updateWidget(it, isPlaying) }
            }
            ACTION_PLAY_PAUSE -> controlPlayer { controller -> if (controller.isPlaying) controller.pause() else controller.play() }
            ACTION_NEXT -> controlPlayer { controller -> if (controller.hasNextMediaItem()) controller.seekToNextMediaItem() }
            ACTION_PREVIOUS -> controlPlayer { controller -> if (controller.hasPreviousMediaItem()) controller.seekToPreviousMediaItem() else controller.seekTo(0) }
        }
        return START_NOT_STICKY
    }

    private fun controlPlayer(action: (MediaController) -> Unit) {
        val token = SessionToken(this, ComponentName(this, MusicPlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener({
            runCatching {
                val controller = future.get()
                action(controller)
                controller.release()
            }
            MediaController.releaseFuture(future)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun updateWidget(song: Song, isPlaying: Boolean) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, MusicWidgetProvider::class.java)
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

            val playPauseIcon = if (isPlaying) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            }
            setImageViewResource(R.id.widget_play_button, playPauseIcon)

            song.albumArtUrl?.let { url ->
                try {
                    val loader = ImageLoader(this@MusicWidgetService)
                    val request = ImageRequest.Builder(this@MusicWidgetService)
                        .data(url)
                        .allowHardware(false)
                        .build()

                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        setImageViewBitmap(R.id.widget_album_art, result.drawable.toBitmap())
                    }
                } catch (_: Exception) {
                    setImageViewResource(R.id.widget_album_art, R.drawable.ic_launcher_foreground)
                }
            } ?: setImageViewResource(R.id.widget_album_art, R.drawable.ic_launcher_foreground)

            val mainPendingIntent = PendingIntent.getActivity(
                this@MusicWidgetService,
                0,
                Intent(this@MusicWidgetService, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

            setOnClickPendingIntent(
                R.id.widget_play_button,
                widgetActionPendingIntent(ACTION_PLAY_PAUSE, 0)
            )
            setOnClickPendingIntent(
                R.id.widget_next_button,
                widgetActionPendingIntent(ACTION_NEXT, 1)
            )
            setOnClickPendingIntent(
                R.id.widget_previous_button,
                widgetActionPendingIntent(ACTION_PREVIOUS, 2)
            )
        }
    }

    private fun widgetActionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, MusicWidgetService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
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

@Suppress("DEPRECATION")
private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        getParcelableExtra(key)
    }
}
