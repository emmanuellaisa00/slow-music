package com.slowmusic.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SlowMusicApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Music Playback Channel
            val playbackChannel = NotificationChannel(
                PLAYBACK_CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
            }

            // Download Channel
            val downloadChannel = NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download progress and completion"
            }

            notificationManager.createNotificationChannels(
                listOf(playbackChannel, downloadChannel)
            )
        }
    }

    companion object {
        const val PLAYBACK_CHANNEL_ID = "slow_music_playback"
        const val DOWNLOAD_CHANNEL_ID = "slow_music_download"
    }
}
