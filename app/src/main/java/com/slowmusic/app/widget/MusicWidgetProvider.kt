package com.slowmusic.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.slowmusic.app.R

class MusicWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }
    
    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
    
    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_music)
            
            // Set up click listeners
            val playIntent = Intent(context, MusicWidgetService::class.java).apply {
                action = MusicWidgetService.ACTION_PLAY
            }
            val playPendingIntent = PendingIntent.getService(
                context, 0, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_play_button, playPendingIntent)
            
            val nextIntent = Intent(context, MusicWidgetService::class.java).apply {
                action = MusicWidgetService.ACTION_NEXT
            }
            val nextPendingIntent = PendingIntent.getService(
                context, 1, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_next_button, nextPendingIntent)
            
            val prevIntent = Intent(context, MusicWidgetService::class.java).apply {
                action = MusicWidgetService.ACTION_PREVIOUS
            }
            val prevPendingIntent = PendingIntent.getService(
                context, 2, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_previous_button, prevPendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

import android.app.PendingIntent

private class PendingIntent {
    companion object {
        fun getService(
            context: Context,
            requestCode: Int,
            intent: Intent,
            flags: Int
        ): PendingIntent {
            return android.app.PendingIntent.getService(
                context, requestCode, intent, flags
            )
        }
    }
}
