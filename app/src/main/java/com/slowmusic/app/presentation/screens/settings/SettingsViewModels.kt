package com.slowmusic.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    val preferences: StateFlow<UserPreferences> = preferencesRepository.getUserPreferences()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferences()
        )
    
    fun updateTheme(theme: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(theme)
        }
    }
    
    fun updateNavigationStyle(style: NavigationStyle) {
        viewModelScope.launch {
            preferencesRepository.setNavigationStyle(style)
        }
    }
    
    fun updateDownloadOnWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            val current = preferences.value
            preferencesRepository.updateUserPreferences(
                current.copy(downloadOnWifiOnly = enabled)
            )
        }
    }
    
    fun updateAutoPlaySimilar(enabled: Boolean) {
        viewModelScope.launch {
            val current = preferences.value
            preferencesRepository.updateUserPreferences(
                current.copy(autoPlaySimilar = enabled)
            )
        }
    }
    
    fun updateCrossfadeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = preferences.value
            preferencesRepository.updateUserPreferences(
                current.copy(crossfadeEnabled = enabled)
            )
        }
    }
    
    fun updateAudioQuality(quality: AudioQuality) {
        viewModelScope.launch {
            val current = preferences.value
            preferencesRepository.updateUserPreferences(
                current.copy(audioQuality = quality)
            )
        }
    }

    fun updateUiStyle(style: UIStyle) {
        viewModelScope.launch {
            val current = preferences.value
            preferencesRepository.updateUserPreferences(current.copy(uiStyle = style))
        }
    }
}

@HiltViewModel
class LogsViewModel @Inject constructor() : ViewModel() {
    
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    init {
        // Add initial log entries for testing
        addLog(LogLevel.INFO, "App", "Slow Music app started")
        addLog(LogLevel.INFO, "Network", "Checking network status...")
        addLog(LogLevel.INFO, "Music", "Loading music library...")
        addLog(LogLevel.INFO, "Preferences", "Theme: Dark, Navigation: Tabs")
        addLog(LogLevel.WARNING, "Network", "Slow connection detected")
        addLog(LogLevel.INFO, "Playback", "Ready to play music")
    }
    
    fun addLog(level: LogLevel, tag: String, message: String) {
        val entry = LogEntry(
            timestamp = dateFormat.format(Date()),
            level = level,
            tag = tag,
            message = message
        )
        _logs.update { it + entry }
    }
    
    fun clearLogs() {
        _logs.value = emptyList()
        addLog(LogLevel.INFO, "System", "Logs cleared")
    }
    
    fun logInfo(tag: String, message: String) {
        addLog(LogLevel.INFO, tag, message)
    }
    
    fun logWarning(tag: String, message: String) {
        addLog(LogLevel.WARNING, tag, message)
    }
    
    fun logError(tag: String, message: String) {
        addLog(LogLevel.ERROR, tag, message)
    }
}
