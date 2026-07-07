package com.slowmusic.app.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowmusic.app.audio.EqualizerManager
import com.slowmusic.app.domain.model.EqualizerSettings
import com.slowmusic.app.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerControlScreen(
    onNavigateBack: () -> Unit,
    viewModel: EqualizerControlViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val presets = viewModel.presets
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 0.dp),
                title = { Text("Equalizer") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp)) {
            item {
                ListItem(
                    headlineContent = { Text("Enable Equalizer") },
                    supportingContent = { Text(if (settings.enabled) "Audio effects active" else "Off") },
                    trailingContent = { Switch(settings.enabled, onCheckedChange = viewModel::setEnabled) }
                )
            }
            item { Text("Presets", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 12.dp)) }
            items(presets.size) { index ->
                ListItem(
                    modifier = Modifier.clickable { viewModel.setPreset(index) },
                    headlineContent = { Text(presets[index]) },
                    trailingContent = { if (settings.preset == index) Text("✓") }
                )
            }
            item { Text("Bands", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 12.dp)) }
            items(5) { band ->
                val value = (settings.bandLevels[band] ?: 50).toFloat()
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text("Band ${band + 1}: ${value.toInt()}%")
                    Slider(
                        value = value,
                        onValueChange = { viewModel.setBand(band, it.toInt()) },
                        valueRange = 0f..100f
                    )
                }
            }
        }
    }
}

@HiltViewModel
class EqualizerControlViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val equalizerManager: EqualizerManager
) : ViewModel() {
    val settings: StateFlow<EqualizerSettings> = preferencesRepository.getEqualizerSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EqualizerSettings())
    val presets: List<String> get() = equalizerManager.presets

    fun setEnabled(enabled: Boolean) = update(settings.value.copy(enabled = enabled))
    fun setPreset(index: Int) = update(settings.value.copy(enabled = true, preset = index))
    fun setBand(band: Int, value: Int) = update(settings.value.copy(enabled = true, bandLevels = settings.value.bandLevels + (band to value)))

    private fun update(newSettings: EqualizerSettings) {
        equalizerManager.apply(newSettings)
        viewModelScope.launch { preferencesRepository.updateEqualizerSettings(newSettings) }
    }
}
