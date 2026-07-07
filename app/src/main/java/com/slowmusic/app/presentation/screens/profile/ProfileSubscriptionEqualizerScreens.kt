package com.slowmusic.app.presentation.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.theme.apple.*
import com.slowmusic.app.presentation.components.apple.*

/**
 * Apple Music Style Profile Screen
 */
@Composable
fun AppleProfileScreen(
    subscription: Subscription,
    onNavigateToSubscription: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppleColors.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(
                subscription = subscription,
                onNavigateToSubscription = onNavigateToSubscription
            )
        }
        
        // Account Section
        item {
            ProfileSection(title = "Account")
        }
        
        item {
            ProfileMenuItem(
                icon = Icons.Filled.CardMembership,
                title = "Manage Subscription",
                subtitle = subscription.type.name,
                onClick = onNavigateToSubscription
            )
        }
        
        item {
            ProfileMenuItem(
                icon = Icons.Filled.Payment,
                title = "Purchase History",
                subtitle = "View your purchases",
                onClick = { }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = Icons.Filled.CardGiftcard,
                title = "Redeem Code",
                subtitle = "Enter a promo code",
                onClick = { }
            )
        }
        
        // Support Section
        item {
            ProfileSection(title = "Support")
        }
        
        item {
            ProfileMenuItem(
                icon = Icons.Filled.Help,
                title = "Help Center",
                subtitle = "Get help with Slow Music",
                onClick = { }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = Icons.Filled.Feedback,
                title = "Send Feedback",
                subtitle = "Help us improve",
                onClick = { }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = Icons.Filled.Info,
                title = "About",
                subtitle = "Version 1.0.0",
                onClick = { }
            )
        }
        
        // Settings
        item {
            ProfileSection(title = "Settings")
        }
        
        item {
            ProfileMenuItem(
                icon = Icons.Filled.Settings,
                title = "App Settings",
                subtitle = "Appearance, playback, downloads",
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    subscription: Subscription,
    onNavigateToSubscription: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(AppleColors.gradientPurple)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            // Premium badge
            if (subscription.type != SubscriptionType.FREE) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AppleColors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Music Lover",
            style = AppleTypography.title2,
            color = AppleColors.textPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = subscription.type.name.lowercase().replaceFirstChar { it.uppercase() },
            style = AppleTypography.subheadline,
            color = AppleColors.textSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Subscription CTA
        if (subscription.type == SubscriptionType.FREE) {
            AppleGlassButton(
                text = "Upgrade to Premium",
                onClick = onNavigateToSubscription,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Premium features preview
            AppleGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                backgroundColor = AppleColors.primary.copy(alpha = 0.1f),
                borderColor = AppleColors.primary.copy(alpha = 0.3f)
            ) {
                Column {
                    subscription.features.take(3).forEach { feature ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = AppleColors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feature,
                                style = AppleTypography.footnote,
                                color = AppleColors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(title: String) {
    Text(
        text = title.uppercase(),
        style = AppleTypography.caption1,
        color = AppleColors.textSecondary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppleColors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppleTypography.body,
                    color = AppleColors.textPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = AppleTypography.footnote,
                        color = AppleColors.textTertiary
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppleColors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Apple Music Style Subscription Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    currentType: SubscriptionType,
    onNavigateBack: () -> Unit,
    onSelectPlan: (SubscriptionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleColors.background)
            .statusBarsPadding()
    ) {
        AppleNavigationBar(
            title = "Subscription",
            onBackClick = onNavigateBack
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choose Your Plan",
                        style = AppleTypography.title1,
                        color = AppleColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Cancel anytime",
                        style = AppleTypography.body,
                        color = AppleColors.textSecondary
                    )
                }
            }
            
            // Plans
            item {
                SubscriptionPlanCard(
                    type = SubscriptionType.FREE,
                    title = "Free",
                    price = "$0",
                    period = "forever",
                    features = listOf(
                        "Ad-supported playback",
                        "Basic search",
                        "Limited skips",
                        "Standard quality"
                    ),
                    isSelected = currentType == SubscriptionType.FREE,
                    onSelect = { onSelectPlan(SubscriptionType.FREE) }
                )
            }
            
            item {
                SubscriptionPlanCard(
                    type = SubscriptionType.PREMIUM,
                    title = "Premium",
                    price = "$9.99",
                    period = "/month",
                    features = listOf(
                        "Ad-free listening",
                        "Unlimited skips",
                        "High quality audio",
                        "Offline downloads",
                        "Lyrics access"
                    ),
                    isSelected = currentType == SubscriptionType.PREMIUM,
                    onSelect = { onSelectPlan(SubscriptionType.PREMIUM) },
                    isPopular = true
                )
            }
            
            item {
                SubscriptionPlanCard(
                    type = SubscriptionType.FAMILY,
                    title = "Family",
                    price = "$15.99",
                    period = "/month",
                    features = listOf(
                        "Up to 6 accounts",
                        "All Premium features",
                        "Family shared playlists",
                        "Explicit content filter"
                    ),
                    isSelected = currentType == SubscriptionType.FAMILY,
                    onSelect = { onSelectPlan(SubscriptionType.FAMILY) }
                )
            }
            
            item {
                SubscriptionPlanCard(
                    type = SubscriptionType.STUDENT,
                    title = "Student",
                    price = "$4.99",
                    period = "/month",
                    features = listOf(
                        "50% discount",
                        "All Premium features",
                        "Valid student ID required"
                    ),
                    isSelected = currentType == SubscriptionType.STUDENT,
                    onSelect = { onSelectPlan(SubscriptionType.STUDENT) }
                )
            }
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    type: SubscriptionType,
    title: String,
    price: String,
    period: String,
    features: List<String>,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isPopular: Boolean = false
) {
    AppleGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        cornerRadius = 20.dp,
        backgroundColor = when {
            isPopular -> AppleColors.primary.copy(alpha = 0.15f)
            isSelected -> AppleColors.primary.copy(alpha = 0.1f)
            else -> AppleColors.glassWhite
        },
        borderColor = when {
            isSelected -> AppleColors.primary
            else -> AppleColors.glassBorder
        }
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = AppleTypography.title3,
                        color = AppleColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isPopular) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AppleColors.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "POPULAR",
                                style = AppleTypography.caption2,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = AppleColors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Price
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = price,
                    style = AppleTypography.title1,
                    color = AppleColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = period,
                    style = AppleTypography.subheadline,
                    color = AppleColors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Features
            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = AppleColors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        style = AppleTypography.footnote,
                        color = AppleColors.textPrimary
                    )
                }
            }
            
            if (type != SubscriptionType.FREE) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPopular) AppleColors.primary else AppleColors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isSelected) "Current Plan" else "Subscribe",
                        style = AppleTypography.headline,
                        color = if (isPopular || isSelected) Color.White else AppleColors.background
                    )
                }
            }
        }
    }
}

/**
 * Apple Music Style Equalizer Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    isEnabled: Boolean,
    presets: List<String>,
    selectedPreset: Int,
    bandLevels: List<Float>,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (Int) -> Unit,
    onBandLevelChange: (Int, Float) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleColors.background)
            .statusBarsPadding()
    ) {
        AppleNavigationBar(
            title = "Equalizer",
            onBackClick = onNavigateBack
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enable/Disable
            item {
                AppleGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Equalizer",
                                style = AppleTypography.headline,
                                color = AppleColors.textPrimary
                            )
                            Text(
                                text = if (isEnabled) "On" else "Off",
                                style = AppleTypography.footnote,
                                color = AppleColors.textSecondary
                            )
                        }
                        
                        AppleToggle(
                            checked = isEnabled,
                            onCheckedChange = onToggleEnabled
                        )
                    }
                }
            }
            
            // Presets
            item {
                Text(
                    text = "PRESETS",
                    style = AppleTypography.caption1,
                    color = AppleColors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                AppleGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Column {
                        presets.forEachIndexed { index, preset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPreset(index) }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = preset,
                                    style = AppleTypography.body,
                                    color = if (selectedPreset == index) 
                                        AppleColors.primary 
                                    else 
                                        AppleColors.textPrimary
                                )
                                
                                if (selectedPreset == index) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = AppleColors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            if (index < presets.lastIndex) {
                                Divider(
                                    color = AppleColors.glassBorder,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Band sliders
            if (isEnabled) {
                item {
                    Text(
                        text = "BAND LEVELS",
                        style = AppleTypography.caption1,
                        color = AppleColors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                item {
                    AppleGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column {
                            bandLevels.forEachIndexed { index, level ->
                                BandSlider(
                                    label = "${(index + 1) * 100}Hz",
                                    value = level,
                                    onValueChange = { onBandLevelChange(index, it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BandSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = AppleTypography.footnote,
                color = AppleColors.textSecondary
            )
            Text(
                text = "${(value * 100).toInt() - 50}%",
                style = AppleTypography.footnote,
                color = AppleColors.textTertiary
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = AppleColors.primary,
                activeTrackColor = AppleColors.primary,
                inactiveTrackColor = AppleColors.textTertiary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}
