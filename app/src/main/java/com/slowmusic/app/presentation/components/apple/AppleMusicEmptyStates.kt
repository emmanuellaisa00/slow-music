package com.slowmusic.app.presentation.components.apple

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

/**
 * Apple Music Style Empty State
 */
@Composable
fun AppleEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")
    
    val floatAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon container
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(y = floatAnimation.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppleColors.primary.copy(alpha = 0.2f),
                            AppleColors.primary.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppleColors.primary,
                modifier = Modifier.size(56.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = title,
            style = AppleTypography.title2,
            color = AppleColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subtitle,
                style = AppleTypography.body,
                color = AppleColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(32.dp))
            AppleGlassButton(
                text = actionLabel,
                onClick = onAction
            )
        }
    }
}

/**
 * Apple Music Style Error State
 */
@Composable
fun AppleErrorState(
    icon: ImageVector = Icons.Filled.ErrorOutline,
    title: String = "Something went wrong",
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error icon with animation
        val rotation by animateFloatAsState(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 500
                0f at 0
                10f at 250
                0f at 500
            },
            label = "shake"
        )
        
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppleColors.secondary.copy(alpha = 0.2f),
                            AppleColors.secondary.copy(alpha = 0.05f)
                        )
                    )
                )
                .graphicsLayer { rotationZ = rotation },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppleColors.secondary,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = title,
            style = AppleTypography.title2,
            color = AppleColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        
        if (message != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = AppleTypography.body,
                color = AppleColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppleColors.primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Try Again",
                    style = AppleTypography.headline
                )
            }
        }
    }
}

/**
 * Apple Music Style Network Error
 */
@Composable
fun AppleNetworkErrorState(
    onRetry: () -> Unit,
    onSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AppleErrorState(
        icon = Icons.Filled.WifiOff,
        title = "No Internet Connection",
        message = "Check your Wi-Fi or mobile data and try again",
        onRetry = onRetry,
        modifier = modifier
    )
}

/**
 * Apple Music Style Not Found
 */
@Composable
fun AppleNotFoundState(
    title: String = "Not Found",
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AppleColors.textTertiary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SearchOff,
                contentDescription = null,
                tint = AppleColors.textTertiary,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = title,
            style = AppleTypography.title2,
            color = AppleColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subtitle,
                style = AppleTypography.body,
                color = AppleColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Apple Music Style Glass Button
 */
@Composable
fun AppleGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppleColors.primary,
            disabledContainerColor = AppleColors.primary.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            style = AppleTypography.headline,
            color = Color.White
        )
    }
}

/**
 * Apple Music Style Shimmer Loading
 */
@Composable
fun AppleShimmerCard(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        AppleColors.glassWhite.copy(alpha = 0.3f),
        AppleColors.glassWhite.copy(alpha = 0.1f),
        AppleColors.glassWhite.copy(alpha = 0.3f)
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppleColors.glassWhite)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.linearGradient(shimmerColors)
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.linearGradient(shimmerColors)
                        )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.linearGradient(shimmerColors)
                        )
                )
            }
        }
    }
}

/**
 * Apple Music Style Loading Screen
 */
@Composable
fun AppleLoadingScreen(
    message: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = AppleColors.primary,
            strokeWidth = 3.dp
        )
        
        if (message != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = AppleTypography.body,
                color = AppleColors.textSecondary
            )
        }
    }
}

/**
 * Empty States for different screens
 */
object AppleEmptyStates {
    @Composable
    fun NoFavorites(onAddFavorite: () -> Unit = {}) {
        AppleEmptyState(
            icon = Icons.Filled.FavoriteBorder,
            title = "No Favorites Yet",
            subtitle = "Songs you like will appear here.\nTap the heart icon to add favorites.",
            actionLabel = "Start Listening",
            onAction = onAddFavorite
        )
    }
    
    @Composable
    fun NoSearchResults(query: String) {
        AppleEmptyState(
            icon = Icons.Filled.SearchOff,
            title = "No Results",
            subtitle = "We couldn't find anything for \"$query\"\nTry a different search."
        )
    }
    
    @Composable
    fun NoDownloads(onBrowse: () -> Unit = {}) {
        AppleEmptyState(
            icon = Icons.Filled.Download,
            title = "No Downloads",
            subtitle = "Download songs to listen offline.\nThey're saved here for you.",
            actionLabel = "Browse Music",
            onAction = onBrowse
        )
    }
    
    @Composable
    fun NoPlaylists(onCreate: () -> Unit = {}) {
        AppleEmptyState(
            icon = Icons.Filled.QueueMusic,
            title = "No Playlists",
            subtitle = "Create your first playlist and organize your favorite songs.",
            actionLabel = "Create Playlist",
            onAction = onCreate
        )
    }
    
    @Composable
    fun NoRecentPlays() {
        AppleEmptyState(
            icon = Icons.Filled.History,
            title = "No Recent Plays",
            subtitle = "Songs you play will appear here.\nStart listening to see your history."
        )
    }
    
    @Composable
    fun NoLocalMusic() {
        AppleEmptyState(
            icon = Icons.Filled.Smartphone,
            title = "No Local Music",
            subtitle = "We couldn't find any music on your device.\nAdd some music files to get started."
        )
    }
    
    @Composable
    fun NoArtists() {
        AppleEmptyState(
            icon = Icons.Filled.Artists,
            title = "No Artists",
            subtitle = "Artists you follow will appear here.\nStart exploring to find your favorites."
        )
    }
    
    @Composable
    fun NoAlbums() {
        AppleEmptyState(
            icon = Icons.Filled.Album,
            title = "No Albums",
            subtitle = "Albums you save will appear here.\nBrowse and save albums you love."
        )
    }
}
