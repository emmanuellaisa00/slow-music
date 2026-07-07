package com.slowmusic.app.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private const val DEFAULT_TAG = "SlowMusic"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val logs = mutableListOf<LogEntry>()

    fun d(message: String) = d(DEFAULT_TAG, message)
    fun d(tag: String, message: String) {
        Log.d(tag, message)
        addToMemoryLog(LogLevel.DEBUG, tag, message)
    }

    fun i(message: String) = i(DEFAULT_TAG, message)
    fun i(tag: String, message: String) {
        Log.i(tag, message)
        addToMemoryLog(LogLevel.INFO, tag, message)
    }

    fun w(message: String) = w(DEFAULT_TAG, message)
    fun w(tag: String, message: String) {
        Log.w(tag, message)
        addToMemoryLog(LogLevel.WARNING, tag, message)
    }

    fun e(message: String, throwable: Throwable? = null) = e(DEFAULT_TAG, message, throwable)
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) "$message\n${throwable.message}" else message
        Log.e(tag, fullMessage, throwable)
        addToMemoryLog(LogLevel.ERROR, tag, fullMessage)
    }

    private fun addToMemoryLog(level: LogLevel, tag: String, message: String) {
        synchronized(logs) {
            logs.add(
                LogEntry(
                    timestamp = dateFormat.format(Date()),
                    level = level,
                    tag = tag,
                    message = message
                )
            )
            if (logs.size > 1000) logs.removeAt(0)
        }
    }

    fun getLogs(): List<LogEntry> = synchronized(logs) { logs.toList() }

    fun clearLogs() {
        synchronized(logs) { logs.clear() }
    }
}

data class LogEntry(
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String
)

enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR
}
