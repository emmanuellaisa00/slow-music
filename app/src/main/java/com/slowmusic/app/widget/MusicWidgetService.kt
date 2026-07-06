package com.slowmusic.app.widget

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MusicWidgetService : Service() {
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                // Handle play/pause
            }
            ACTION_NEXT -> {
                // Handle next
            }
            ACTION_PREVIOUS -> {
                // Handle previous
            }
        }
        return START_NOT_STICKY
    }
    
    companion object {
        const val ACTION_PLAY = "com.slowmusic.app.widget.PLAY"
        const val ACTION_NEXT = "com.slowmusic.app.widget.NEXT"
        const val ACTION_PREVIOUS = "com.slowmusic.app.widget.PREVIOUS"
    }
}
