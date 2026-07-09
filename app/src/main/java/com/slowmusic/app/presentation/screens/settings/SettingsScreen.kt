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
    var dialog by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var resolverText by remember(preferences.resolverBackendUrl) { mutableStateOf(preferences.resolverBackendUrl) }

    when (dialog) {
        "theme" -> ChoiceDialog("Theme", ThemeMode.values().map { it.name }, { viewModel.updateTheme(ThemeMode.valueOf(it)); dialog = null }, { dialog = null })
        "nav" -> ChoiceDialog("Navigation", NavigationStyle.values().map { it.name }, { viewModel.updateNavigationStyle(NavigationStyle.valueOf(it)); dialog = null }, { dialog = null })
        "ui" -> ChoiceDialog("UI Style", UIStyle.values().map { it.name }, { viewModel.updateUiStyle(UIStyle.valueOf(it)); dialog = null }, { dialog = null })
        "quality" -> ChoiceDialog("Audio Quality", AudioQuality.values().map { it.name }, { viewModel.updateAudioQuality(AudioQuality.valueOf(it)); dialog = null }, { dialog = null })
        "crossfade" -> ChoiceDialog("Crossfade", listOf("Off", "On"), { viewModel.updateCrossfadeEnabled(it == "On"); dialog = null }, { dialog = null })
        "speed" -> ChoiceDialog("Playback Speed", listOf("0.75x", "Normal", "1.25x", "1.5x"), {
            val speed = when (it) { "0.75x" -> 0.75f; "1.25x" -> 1.25f; "1.5x" -> 1.5f; else -> 1f }
            viewModel.updatePlaybackSpeed(speed)
            message = "Playback speed set to $it"
            dialog = null
        }, { dialog = null })
        "network" -> ChoiceDialog("Network Mode", NetworkMode.values().map { it.name }, { viewModel.updateNetworkMode(NetworkMode.valueOf(it)); dialog = null }, { dialog = null })
        "resolver" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Resolver backend") },
            text = { OutlinedTextField(value = resolverText, onValueChange = { resolverText = it }, label = { Text("Backend URL") }, placeholder = { Text("https://your-worker.example.com") }) },
            confirmButton = { TextButton(onClick = { viewModel.updateResolverBackend(resolverText); message = "Resolver backend saved"; dialog = null }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { resolverText = ""; viewModel.updateResolverBackend(""); message = "Resolver backend disabled"; dialog = null }) { Text("Disable") } }
        )
        "about" -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Slow Music") }, text = { Text("Version 1.0.0\nLocal database mode enabled\nOnline catalog powered by iTunes Search API") }, confirmButton = { TextButton(onClick = { dialog = null }) { Text("OK") } })
        "clear" -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Clear cache?") }, text = { Text("This clears cached Home/Search metadata so the next refresh fetches fresh content.") }, confirmButton = { TextButton(onClick = { viewModel.clearCache { message = it }; dialog = null }) { Text("Clear") } }, dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } })
        "reset" -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Reset app data?") }, text = { Text("This clears favorites, downloads, playlists, followed artists, search history, cached content, and preferences.") }, confirmButton = { TextButton(onClick = { viewModel.resetAppData { message = it }; dialog = null }) { Text("Reset") } }, dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 0.dp),
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
                    subtitle = if (preferences.uiStyle == UIStyle.APPLE_MUSIC) "Apple Music" else "Default",
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
                        Switch(
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
                        Switch(
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
                    title = "Resolver Backend",
                    subtitle = preferences.resolverBackendUrl.ifBlank { "Disabled" },
                    onClick = { resolverText = preferences.resolverBackendUrl; dialog = "resolver" }
                )
            }

            // Subscription
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Subscription")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.CardMembership,
                    title = "Subscription",
                    subtitle = "Local database mode: plan management disabled",
                    onClick = { message = "Subscriptions are disabled while using local database mode" }
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

@Composable
private fun ChoiceDialog(
    title: String,
    choices: List<String>,
    onChoose: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                choices.forEach { choice ->
                    ListItem(
                        modifier = Modifier.clickable { onChoose(choice) },
                        headlineContent = { Text(choice.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun SettingsSection(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = PrimaryGreen,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
    val contentColor = if (isDanger) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    ListItem(
        modifier = (if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)),
        headlineContent = {
            Text(
                text = title,
                color = contentColor
            )
        },
        supportingContent = subtitle?.let {
            { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
        },
        trailingContent = trailing
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
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 0.dp),
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
