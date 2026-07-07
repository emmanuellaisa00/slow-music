package com.slowmusic.app.presentation.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.theme.apple.*
import com.slowmusic.app.presentation.components.apple.*

/**
 * Apple Music-style Settings Screen
 * Features glassmorphism, liquid glass, dynamic colors, and smooth animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleMusicSettingsScreen(
    preferences: UserPreferences,
    onPreferenceChange: (UserPreferences) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleColors.background)
            .statusBarsPadding()
    ) {
        // Navigation Bar
        AppleNavigationBar(
            title = "Settings",
            onBackClick = onNavigateBack,
            trailing = {
                Text(
                    text = "Done",
                    style = AppleTypography.headline,
                    color = AppleColors.primary,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { onNavigateBack() }
                )
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 🔮 APPEARANCE SECTION
            item {
                AppleSectionHeader(title = "Appearance")
            }
            
            item {
                AnimatedListItem(visible = true, delayMillis = 0) {
                    AppleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Theme
                        AppleSettingsRow(
                            title = "Theme",
                            value = when (preferences.theme) {
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.SYSTEM -> "System"
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Palette,
                                    contentDescription = null,
                                    tint = AppleColors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Navigation Style
                        AppleSettingsRow(
                            title = "Navigation",
                            value = when (preferences.navigationStyle) {
                                NavigationStyle.TABS -> "Tabs"
                                NavigationStyle.BOTTOM_NAV -> "Bottom Bar"
                                NavigationStyle.DRAWER -> "Drawer"
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Navigation,
                                    contentDescription = null,
                                    tint = AppleColors.accent,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                    }
                }
            }
            
            // 🎨 APPLE MUSIC UI STYLE SECTION
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppleSectionHeader(title = "Apple Music Style")
            }
            
            item {
                AnimatedListItem(visible = true, delayMillis = 50) {
                    AppleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Glassmorphism
                        AppleSettingsRow(
                            title = "Glassmorphism",
                            subtitle = "Frosted glass effects",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Layers,
                                    contentDescription = null,
                                    tint = AppleColors.gradientPurple.first(),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            trailing = {
                                AppleToggle(
                                    checked = true,
                                    onCheckedChange = { }
                                )
                            }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Liquid Glass
                        AppleSettingsRow(
                            title = "Liquid Glass",
                            subtitle = "Soft gradient overlays",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Water,
                                    contentDescription = null,
                                    tint = AppleColors.gradientBlue.first(),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            trailing = {
                                AppleToggle(
                                    checked = true,
                                    onCheckedChange = { }
                                )
                            }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Dynamic Colors
                        AppleSettingsRow(
                            title = "Dynamic Colors",
                            subtitle = "Extract colors from album art",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.ColorLens,
                                    contentDescription = null,
                                    tint = AppleColors.gradientPink.first(),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            trailing = {
                                AppleToggle(
                                    checked = preferences.navigationStyle != NavigationStyle.SYSTEM,
                                    onCheckedChange = { }
                                )
                            }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // 120Hz Animations
                        AppleSettingsRow(
                            title = "Smooth Animations",
                            subtitle = "120Hz spring animations",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = null,
                                    tint = AppleColors.gradientOrange.first(),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            trailing = {
                                AppleToggle(
                                    checked = true,
                                    onCheckedChange = { }
                                )
                            }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Corner Radius
                        AppleSettingsRow(
                            title = "Rounded Corners",
                            value = "24dp",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.RoundedCorner,
                                    contentDescription = null,
                                    tint = AppleColors.gradientGreen.first(),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                    }
                }
            }
            
            // 🎵 PLAYBACK SECTION
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppleSectionHeader(title = "Playback")
            }
            
            item {
                AnimatedListItem(visible = true, delayMillis = 100) {
                    AppleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Crossfade
                        AppleSettingsRow(
                            title = "Crossfade",
                            value = if (preferences.crossfadeEnabled) "${preferences.crossfadeDuration}s" else "Off",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.SwapHoriz,
                                    contentDescription = null,
                                    tint = AppleColors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Playback Speed
                        AppleSettingsRow(
                            title = "Playback Speed",
                            value = "Normal",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = null,
                                    tint = AppleColors.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Auto-play
                        AppleSettingsRow(
                            title = "Auto-Play Similar",
                            subtitle = "Queue similar songs",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.PlayCircle,
                                    contentDescription = null,
                                    tint = AppleColors.tertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            trailing = {
                                AppleToggle(
                                    checked = preferences.autoPlaySimilar,
                                    onCheckedChange = { onPreferenceChange(preferences.copy(autoPlaySimilar = it)) }
                                )
                            }
                        )
                    }
                }
            }
            
            // 🎛️ AUDIO SECTION
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppleSectionHeader(title = "Audio")
            }
            
            item {
                AnimatedListItem(visible = true, delayMillis = 150) {
                    AppleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Equalizer
                        AppleSettingsRow(
                            title = "Equalizer",
                            subtitle = "Customize audio",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Equalizer,
                                    contentDescription = null,
                                    tint = AppleColors.gradientPurple.first(),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Audio Quality
                        AppleSettingsRow(
                            title = "Audio Quality",
                            value = preferences.audioQuality.name,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.HighQuality,
                                    contentDescription = null,
                                    tint = AppleColors.gradientBlue.first(),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Audio Focus
                        AppleSettingsRow(
                            title = "Audio Focus",
                            subtitle = "Handle interruptions",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Hearing,
                                    contentDescription = null,
                                    tint = AppleColors.gradientOrange.first(),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            trailing = {
                                AppleToggle(
                                    checked = true,
                                    onCheckedChange = { }
                                )
                            }
                        )
                    }
                }
            }
            
            // 📥 DOWNLOADS SECTION
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppleSectionHeader(title = "Downloads")
            }
            
            item {
                AnimatedListItem(visible = true, delayMillis = 200) {
                    AppleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Wi-Fi Only
                        AppleSettingsRow(
                            title = "Download on Wi-Fi",
                            subtitle = "Save mobile data",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Wifi,
                                    contentDescription = null,
                                    tint = AppleColors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            trailing = {
                                AppleToggle(
                                    checked = preferences.downloadOnWifiOnly,
                                    onCheckedChange = { onPreferenceChange(preferences.copy(downloadOnWifiOnly = it)) }
                                )
                            }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Storage
                        AppleSettingsRow(
                            title = "Storage",
                            subtitle = "0 MB used",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Storage,
                                    contentDescription = null,
                                    tint = AppleColors.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                    }
                }
            }
            
            // 🌐 NETWORK SECTION
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppleSectionHeader(title = "Network")
            }
            
            item {
                AnimatedListItem(visible = true, delayMillis = 250) {
                    AppleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Network Mode
                        AppleSettingsRow(
                            title = "Network Mode",
                            value = "Smart",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.NetworkCheck,
                                    contentDescription = null,
                                    tint = AppleColors.gradientPink.first(),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                    }
                }
            }
            
            // 🧪 TESTING SECTION
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppleSectionHeader(title = "Testing")
            }
            
            item {
                AnimatedListItem(visible = true, delayMillis = 300) {
                    AppleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Logs
                        AppleSettingsRow(
                            title = "App Logs",
                            subtitle = "View debug logs",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.BugReport,
                                    contentDescription = null,
                                    tint = AppleColors.gradientGreen.first(),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = onNavigateToLogs
                        )
                    }
                }
            }
            
            // 💎 SUBSCRIPTION SECTION
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppleSectionHeader(title = "Subscription")
            }
            
            item {
                AnimatedListItem(visible = true, delayMillis = 350) {
                    AppleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        backgroundColor = AppleColors.primary.copy(alpha = 0.15f),
                        borderColor = AppleColors.primary.copy(alpha = 0.3f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Slow Music Premium",
                                    style = AppleTypography.headline,
                                    color = AppleColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ad-free • Offline • High Quality",
                                    style = AppleTypography.footnote,
                                    color = AppleColors.textSecondary
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppleColors.primary)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clickable { }
                            ) {
                                Text(
                                    text = "Upgrade",
                                    style = AppleTypography.subheadline,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            // ℹ️ ABOUT SECTION
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppleSectionHeader(title = "About")
            }
            
            item {
                AnimatedListItem(visible = true, delayMillis = 400) {
                    AppleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Version
                        AppleSettingsRow(
                            title = "Version",
                            value = "1.0.0",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = AppleColors.textSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Privacy Policy
                        AppleSettingsRow(
                            title = "Privacy Policy",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.PrivacyTip,
                                    contentDescription = null,
                                    tint = AppleColors.textSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Terms of Service
                        AppleSettingsRow(
                            title = "Terms of Service",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = null,
                                    tint = AppleColors.textSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                    }
                }
            }
            
            // DANGER ZONE
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppleSectionHeader(title = "Danger Zone")
            }
            
            item {
                AnimatedListItem(visible = true, delayMillis = 450) {
                    AppleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        backgroundColor = AppleColors.secondary.copy(alpha = 0.1f),
                        borderColor = AppleColors.secondary.copy(alpha = 0.3f)
                    ) {
                        // Clear Cache
                        AppleSettingsRow(
                            title = "Clear Cache",
                            subtitle = "Free up storage",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.DeleteForever,
                                    contentDescription = null,
                                    tint = AppleColors.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                        
                        Divider(
                            color = AppleColors.glassBorder,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Reset App
                        AppleSettingsRow(
                            title = "Reset App",
                            subtitle = "Clear all data",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.RestartAlt,
                                    contentDescription = null,
                                    tint = AppleColors.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
