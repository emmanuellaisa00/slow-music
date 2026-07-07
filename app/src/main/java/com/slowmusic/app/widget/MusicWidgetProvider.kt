package com.slowmusic.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.slowmusic.app.R
import com.slowmusic.app.service.MusicWidgetService

class MusicWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_music).apply {
                setOnClickPendingIntent(
                    R.id.widget_play_button,
                    actionPendingIntent(context, MusicWidgetService.ACTION_PLAY_PAUSE, 0)
                )
                setOnClickPendingIntent(
                    R.id.widget_next_button,
                    actionPendingIntent(context, MusicWidgetService.ACTION_NEXT, 1)
                )
                setOnClickPendingIntent(
                    R.id.widget_previous_button,
                    actionPendingIntent(context, MusicWidgetService.ACTION_PREVIOUS, 2)
                )
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun actionPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, MusicWidgetService::class.java).apply { this.action = action }
            return PendingIntent.getService(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
