package com.slowmusic.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeUtils {
    
    fun formatDuration(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%d:%02d", minutes, seconds)
    }
    
    fun formatDurationLong(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
    
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    fun formatDateShort(dateString: String?): String {
        if (dateString == null) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            dateString.take(10)
        }
    }
}

object NetworkUtils {
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}

object StringUtils {
    
    fun truncate(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            text.take(maxLength - 3) + "..."
        } else {
            text
        }
    }
    
    fun capitalize(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
