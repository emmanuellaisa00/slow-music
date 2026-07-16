package com.slowmusic.app.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogs: () -> Unit = {},
    onNavigateToStorage: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToLocalFilesPermission: () -> Unit = {},
    onNavigateToCastDevices: () -> Unit = {},
    onNavigateToEqualizer: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsState()
    val useStyledUi = preferences.uiStyle != UIStyle.DEFAULT
    var dialog by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var resolverText by remember(preferences.resolverBackendUrl) { mutableStateOf(preferences.resolverBackendUrl) }
    var stemBackendText by remember(preferences.stemSeparationBackendUrl) { mutableStateOf(preferences.stemSeparationBackendUrl) }

    when (dialog) {
        "theme" -> ChoiceSheet("Theme", themeOptions(), preferences.theme.name, { viewModel.updateTheme(ThemeMode.valueOf(it)); dialog = null }, { dialog = null })
        "nav" -> ChoiceSheet("Navigation", navigationOptions(), preferences.navigationStyle.name, { viewModel.updateNavigationStyle(NavigationStyle.valueOf(it)); dialog = null }, { dialog = null })
        "ui" -> ChoiceSheet("UI Style", uiStyleOptions(), preferences.uiStyle.name, { viewModel.updateUiStyle(UIStyle.valueOf(it)); dialog = null }, { dialog = null })
        "quality" -> ChoiceSheet("Audio Quality", audioQualityOptions(), preferences.audioQuality.name, { viewModel.updateAudioQuality(AudioQuality.valueOf(it)); dialog = null }, { dialog = null })
        "crossfade" -> ChoiceSheet("Crossfade", crossfadeOptions(), if (preferences.crossfadeEnabled) "On" else "Off", { viewModel.updateCrossfadeEnabled(it == "On"); dialog = null }, { dialog = null })
        "speed" -> ChoiceSheet("Playback Speed", playbackSpeedOptions(), when (preferences.playbackSpeed) { 0.75f -> "0.75x"; 1.25f -> "1.25x"; 1.5f -> "1.5x"; else -> "Normal" }, {
            val speed = when (it) { "0.75x" -> 0.75f; "1.25x" -> 1.25f; "1.5x" -> 1.5f; else -> 1f }
            viewModel.updatePlaybackSpeed(speed)
            message = "Playback speed set to $it"
            dialog = null
        }, { dialog = null })
        "network" -> ChoiceSheet("Network Mode", networkModeOptions(), preferences.networkMode.name, { viewModel.updateNetworkMode(NetworkMode.valueOf(it)); dialog = null }, { dialog = null })
        "resolver" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Streaming backend") },
            text = { OutlinedTextField(value = resolverText, onValueChange = { resolverText = it }, label = { Text("Backend URL") }, placeholder = { Text("https://your-worker.example.com") }) },
            confirmButton = { TextButton(onClick = { viewModel.updateResolverBackend(resolverText); message = "Streaming backend saved"; dialog = null }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { resolverText = ""; viewModel.updateResolverBackend(""); message = "Streaming backend disabled"; dialog = null }) { Text("Disable") } }
        )
        "stems" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Stem separation backend") },
            text = {
                Column {
                    Text("Required for true Vocals and Instrumental isolation. Endpoint must support POST /stems/resolve and return vocalsUrl/instrumentalUrl.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = stemBackendText, onValueChange = { stemBackendText = it }, label = { Text("Backend URL") }, placeholder = { Text("https://your-stems.example.com") })
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.updateStemSeparationBackend(stemBackendText); message = "Stem backend saved"; dialog = null }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { stemBackendText = ""; viewModel.updateStemSeparationBackend(""); message = "Stem backend disabled"; dialog = null }) { Text("Disable") } }
        )
        "about" -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Slow Music") }, text = { Text("Version 1.0.0\nLocal library mode enabled\nCached discovery and full-song playback") }, confirmButton = { TextButton(onClick = { dialog = null }) { Text("OK") } })
        "clear" -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Clear cache?") }, text = { Text("This clears cached Home/Search metadata so the next refresh fetches fresh content.") }, confirmButton = { TextButton(onClick = { viewModel.clearCache { message = it }; dialog = null }) { Text("Clear") } }, dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } })
        "reset" -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Reset app data?") }, text = { Text("This clears favorites, downloads, playlists, followed artists, search history, cached content, and preferences.") }, confirmButton = { TextButton(onClick = { viewModel.resetAppData { message = it }; dialog = null }) { Text("Reset") } }, dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } })
    }

    Scaffold(
        containerColor = if (useStyledUi) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (useStyledUi) Color.Transparent else MaterialTheme.colorScheme.background,
                    scrolledContainerColor = if (useStyledUi) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else MaterialTheme.colorScheme.surface
                ),
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Appearance
            item {
                SettingsSection(title = "Appearance")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Palette,
                    title = "Theme",
                    subtitle = preferences.theme.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { dialog = "theme" }
                )
            }


            item {
                SettingsItem(
                    icon = Icons.Filled.LibraryMusic,
                    title = "UI Style",
                    subtitle = when (preferences.uiStyle) {
                        UIStyle.DEFAULT -> "Default Android"
                        UIStyle.APPLE_MUSIC -> "Apple Music"
                        UIStyle.IOS_GLASS -> "iOS Glass"
                    },
                    onClick = { dialog = "ui" }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Navigation,
                    title = "Navigation Style",
                    subtitle = when (preferences.navigationStyle) {
                        NavigationStyle.TABS -> "Tabs (Default)"
                        NavigationStyle.BOTTOM_NAV -> "Bottom Navigation"
                        NavigationStyle.DRAWER -> "Drawer"
                    },
                    onClick = { dialog = "nav" }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.BlurOn,
                    title = "Cover Art Background",
                    subtitle = if (preferences.enableCoverArtBlur) "Blurred artwork behind screens" else "Use the normal app background",
                    trailing = {
                        PremiumSwitch(
                            checked = preferences.enableCoverArtBlur,
                            onCheckedChange = { viewModel.updateCoverArtBlur(it) }
                        )
                    }
                )
            }

            // Playback
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Playback")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.PlayCircle,
                    title = "Crossfade",
                    subtitle = if (preferences.crossfadeEnabled) "${preferences.crossfadeDuration}s" else "Off",
                    onClick = { dialog = "crossfade" }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Equalizer,
                    title = "Equalizer",
                    subtitle = "Adjust audio settings",
                    onClick = onNavigateToEqualizer
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Speed,
                    title = "Playback Speed",
                    subtitle = "${preferences.playbackSpeed}x",
                    onClick = { dialog = "speed" }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Autorenew,
                    title = "Auto-Play Similar",
                    subtitle = if (preferences.autoPlaySimilar) "On" else "Off",
                    trailing = {
                        PremiumSwitch(
                            checked = preferences.autoPlaySimilar,
                            onCheckedChange = { viewModel.updateAutoPlaySimilar(it) }
                        )
                    }
                )
            }

            // Downloads
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Downloads")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Wifi,
                    title = "Download on Wi-Fi Only",
                    subtitle = "Save mobile data",
                    trailing = {
                        PremiumSwitch(
                            checked = preferences.downloadOnWifiOnly,
                            onCheckedChange = { viewModel.updateDownloadOnWifiOnly(it) }
                        )
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.HighQuality,
                    title = "Audio Quality",
                    subtitle = preferences.audioQuality.name,
                    onClick = { dialog = "quality" }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Storage,
                    title = "Storage",
                    subtitle = "Manage downloaded music",
                    onClick = onNavigateToStorage
                )
            }

            // Network
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Network")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.NetworkCheck,
                    title = "Network Mode",
                    subtitle = preferences.networkMode.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { dialog = "network" }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Cloud,
                    title = "Streaming Backend",
                    subtitle = preferences.resolverBackendUrl.ifBlank { "Disabled" },
                    onClick = { resolverText = preferences.resolverBackendUrl; dialog = "resolver" }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.GraphicEq,
                    title = "Stem Separation Backend",
                    subtitle = preferences.stemSeparationBackendUrl.ifBlank { "Required for real vocals/instrumental" },
                    onClick = { stemBackendText = preferences.stemSeparationBackendUrl; dialog = "stems" }
                )
            }

            // Local mode
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Library Mode")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Storage,
                    title = "Local Library Mode",
                    subtitle = "Play history, playlists and cache stay on this device",
                    onClick = { message = "Local library mode is active" }
                )
            }

            // About
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "About")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.BugReport,
                    title = "Logs",
                    subtitle = "View app logs for testing",
                    onClick = onNavigateToLogs
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About Slow Music",
                    subtitle = "Version 1.0.0",
                    onClick = { dialog = "about" }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Policy,
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    onClick = onNavigateToPrivacy
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Description,
                    title = "Terms of Service",
                    subtitle = "Read our terms",
                    onClick = onNavigateToTerms
                )
            }

            // Permissions
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Permissions")
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    subtitle = "Allow playback and download alerts",
                    onClick = onNavigateToNotifications
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Folder,
                    title = "Local music access",
                    subtitle = "Explain audio file permission",
                    onClick = onNavigateToLocalFilesPermission
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Cast,
                    title = "Connected devices",
                    subtitle = "Cast and transfer playback",
                    onClick = onNavigateToCastDevices
                )
            }

            // Danger Zone
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Danger Zone")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.DeleteForever,
                    title = "Clear Cache",
                    subtitle = "Free up storage space",
                    onClick = { dialog = "clear" },
                    isDanger = true
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Logout,
                    title = "Reset App",
                    subtitle = "Clear all data and restart",
                    onClick = { dialog = "reset" },
                    isDanger = true
                )
            }
        }
        message?.let { text ->
            LaunchedEffect(text) {
                kotlinx.coroutines.delay(2200)
                message = null
            }
            Snackbar(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text(text) }
        }
    }
}

private data class SettingsChoiceOption(
    val value: String,
    val title: String,
    val subtitle: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceSheet(
    title: String,
    options: List<SettingsChoiceOption>,
    selectedChoice: String,
    onChoose: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 10.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Choose how this setting should behave.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            options.forEach { option ->
                val selected = option.value == selectedChoice
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onChoose(option.value)
                        }
                        .padding(vertical = 3.dp),
                    headlineContent = {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    supportingContent = option.subtitle?.let { subtitle ->
                        {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingContent = {
                        RadioButton(
                            selected = selected,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onChoose(option.value)
                            }
                        )
                    },
                    trailingContent = {
                        if (selected) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.46f) else Color.Transparent,
                        headlineColor = MaterialTheme.colorScheme.onSurface,
                        supportingColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

private fun themeOptions() = listOf(
    SettingsChoiceOption(ThemeMode.LIGHT.name, "Light", "Bright interface for daytime listening."),
    SettingsChoiceOption(ThemeMode.DARK.name, "Dark", "Deep dark theme for music-first focus."),
    SettingsChoiceOption(ThemeMode.SYSTEM.name, "System", "Follow your device appearance setting.")
)

private fun uiStyleOptions() = listOf(
    SettingsChoiceOption(UIStyle.DEFAULT.name, "Default", "Clean Material Design."),
    SettingsChoiceOption(UIStyle.APPLE_MUSIC.name, "Apple Music", "Artwork-inspired premium interface."),
    SettingsChoiceOption(UIStyle.IOS_GLASS.name, "iOS Glass", "Frosted glass appearance with iPhone-style polish.")
)

private fun navigationOptions() = listOf(
    SettingsChoiceOption(NavigationStyle.TABS.name, "Tabs", "Fast primary navigation with saved tab state."),
    SettingsChoiceOption(NavigationStyle.BOTTOM_NAV.name, "Bottom Navigation", "Classic bottom bar layout."),
    SettingsChoiceOption(NavigationStyle.DRAWER.name, "Drawer", "Compact navigation for larger libraries.")
)

private fun audioQualityOptions() = AudioQuality.values().map { quality ->
    SettingsChoiceOption(quality.name, quality.name.lowercase().replaceFirstChar { it.uppercase() }, when (quality.name) {
        "LOW" -> "Uses less data while streaming."
        "MEDIUM" -> "Balanced quality and data usage."
        "HIGH" -> "Better detail for headphones and speakers."
        "LOSSLESS" -> "Best available quality when supported."
        else -> "Audio quality preference."
    })
}

private fun crossfadeOptions() = listOf(
    SettingsChoiceOption("Off", "Off", "Songs change immediately with no overlap."),
    SettingsChoiceOption("On", "On", "Smoothly blend the end of one song into the next.")
)

private fun playbackSpeedOptions() = listOf(
    SettingsChoiceOption("0.75x", "0.75x", "Slower playback for relaxed listening."),
    SettingsChoiceOption("Normal", "Normal", "Original song speed."),
    SettingsChoiceOption("1.25x", "1.25x", "Slightly faster playback."),
    SettingsChoiceOption("1.5x", "1.5x", "Fast playback for spoken or long-form audio.")
)

private fun networkModeOptions() = NetworkMode.values().map { mode ->
    SettingsChoiceOption(mode.name, mode.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, when (mode.name) {
        "ONLINE_ONLY" -> "Always stream when a connection is available."
        "SMART_CACHING" -> "Balance streaming with cached metadata and content."
        "DOWNLOAD_MODE" -> "Prefer downloaded and cached music for offline use."
        else -> "Network behavior preference."
    })
}

@Composable
private fun SettingsSection(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = PrimaryGreen.copy(alpha = 0.78f),
        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    isDanger: Boolean = false
) {
    val haptics = LocalHapticFeedback.current
    val contentColor = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (onClick != null) Modifier.clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.76f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background((if (isDanger) MaterialTheme.colorScheme.error else PrimaryGreen).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (isDanger) MaterialTheme.colorScheme.error else PrimaryGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = contentColor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(12.dp))
                trailing()
            } else if (onClick != null) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f), modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun PremiumSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val haptics = LocalHapticFeedback.current
    Switch(
        checked = checked,
        onCheckedChange = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onCheckedChange(it)
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = PrimaryGreen,
            checkedBorderColor = PrimaryGreen,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.surface,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onNavigateBack: () -> Unit,
    viewModel: LogsViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("App Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(logs.size) { index ->
                val log = logs[logs.size - 1 - index] // Show newest first
                LogItem(log = log)
            }
        }
    }
}

@Composable
private fun LogItem(log: LogEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (log.level) {
                LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer
                LogLevel.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = log.tag,
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryGreen
                )
                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall
            )
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
    INFO, WARNING, ERROR
}

@Composable
fun AppleMusicSettingsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToLogs: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsState()
    AppleMusicSettingsScreen(
        preferences = preferences,
        onPreferenceChange = { viewModel.updatePreferences(it) },
        onNavigateBack = onNavigateBack,
        onNavigateToLogs = onNavigateToLogs
    )
}
