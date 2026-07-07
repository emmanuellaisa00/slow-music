package com.slowmusic.app.presentation.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSubscription: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val subscription by viewModel.subscription.collectAsState()
    var message by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
            // Profile Header
            item {
                ProfileHeader(subscription = subscription)
            }
            
            // Subscription Card
            item {
                SubscriptionCard(
                    subscription = subscription,
                    onUpgradeClick = onNavigateToSubscription
                )
            }
            
            // Account Section
            item {
                ProfileSection(title = "Account")
            }
            
            item {
                ProfileListItem(
                    icon = Icons.Filled.CardMembership,
                    title = "Subscription",
                    subtitle = subscription.type.name,
                    onClick = onNavigateToSubscription
                )
            }
            
            item {
                ProfileListItem(
                    icon = Icons.Filled.Payment,
                    title = "Purchase History",
                    subtitle = "Local database mode",
                    onClick = { message = "Purchase history is disabled in local database mode" }
                )
            }
            
            item {
                ProfileListItem(
                    icon = Icons.Filled.CardGiftcard,
                    title = "Redeem Code",
                    subtitle = "Local database mode",
                    onClick = { message = "Redeem codes are disabled in local database mode" }
                )
            }
            
            // Support Section
            item {
                ProfileSection(title = "Support")
            }
            
            item {
                ProfileListItem(
                    icon = Icons.Filled.Help,
                    title = "Help Center",
                    subtitle = "Support articles coming soon",
                    onClick = { message = "Help Center URL is not configured yet" }
                )
            }
            
            item {
                ProfileListItem(
                    icon = Icons.Filled.Feedback,
                    title = "Send Feedback",
                    subtitle = "Feedback channel coming soon",
                    onClick = { message = "Feedback channel is not configured yet" }
                )
            }
            
            item {
                ProfileListItem(
                    icon = Icons.Filled.Info,
                    title = "About",
                    subtitle = "Version 1.0.0",
                    onClick = { message = "Slow Music v1.0.0 • Local database mode" }
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
private fun ProfileHeader(
    subscription: Subscription
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = PrimaryGreen
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Profile",
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Music Lover",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = subscription.type.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SubscriptionCard(
    subscription: Subscription,
    onUpgradeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (subscription.type) {
                        SubscriptionType.FREE -> "Free Account"
                        SubscriptionType.PREMIUM -> "Premium"
                        SubscriptionType.FAMILY -> "Family"
                        SubscriptionType.STUDENT -> "Student"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (subscription.type) {
                    SubscriptionType.FREE -> "Upgrade to Premium for ad-free listening and more"
                    SubscriptionType.PREMIUM -> "Enjoy all Premium features"
                    SubscriptionType.FAMILY -> "Share with up to 6 family members"
                    SubscriptionType.STUDENT -> "50% off Premium"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (subscription.type == SubscriptionType.FREE) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upgrade to Premium")
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ProfileListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.padding(horizontal = 16.dp).clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                Icons.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    )
}
