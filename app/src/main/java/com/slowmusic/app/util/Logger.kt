package com.slowmusic.app.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    private const val TAG = "SlowMusic"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    // In-memory log storage for the Logs screen
    private val logs = mutableListOf<LogEntry>()
    
    fun d(message: String, tag: String = TAG) {
        Log.d(tag, message)
        addToMemoryLog(LogLevel.DEBUG, tag, message)
    }
    
    fun i(message: String, tag: String = TAG) {
        Log.i(tag, message)
        addToMemoryLog(LogLevel.INFO, tag, message)
    }
    
    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
        addToMemoryLog(LogLevel.WARNING, tag, message)
    }
    
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        val fullMessage = if (throwable != null) {
            "$message\n${throwable.message}"
        } else {
            message
        }
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
            // Keep only last 1000 logs
            if (logs.size > 1000) {
                logs.removeAt(0)
            }
        }
    }
    
    fun getLogs(): List<LogEntry> {
        synchronized(logs) {
            return logs.toList()
        }
    }
    
    fun clearLogs() {
        synchronized(logs) {
            logs.clear()
        }
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
