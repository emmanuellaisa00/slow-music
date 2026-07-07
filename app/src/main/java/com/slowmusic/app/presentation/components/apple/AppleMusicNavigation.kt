package com.slowmusic.app.presentation.components.apple

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.theme.apple.*
import coil.compose.AsyncImage

/**
 * Apple Music Style Bottom Navigation Bar
 */
@Composable
fun AppleBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<AppleNavItem> = appleNavItems
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppleColors.background.copy(alpha = 0.95f),
                        AppleColors.background
                    )
                )
            )
    ) {
        // Glass effect border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AppleColors.glassBorder,
                            Color.Transparent
                        )
                    )
                )
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                AppleNavItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun AppleNavItem(
    item: AppleNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_scale"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = tween(200),
        label = "nav_alpha"
    )
    
    Column(
        modifier = Modifier
            .scale(animatedScale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer { alpha = animatedAlpha },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = if (isSelected) AppleColors.primary else AppleColors.textSecondary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = item.label,
            style = AppleTypography.caption2,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) AppleColors.primary else AppleColors.textSecondary
        )
        
        // Active indicator
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(AppleColors.primary)
            )
        }
    }
}

data class AppleNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badge: Int? = null
)

val appleNavItems = listOf(
    AppleNavItem(
        route = "home",
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    AppleNavItem(
        route = "search",
        label = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    AppleNavItem(
        route = "library",
        label = "Library",
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic
    ),
    AppleNavItem(
        route = "profile",
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)


private enum class SwipeToDismissBoxValue { Settled, StartToEnd, EndToStart }
private class SimpleSwipeToDismissBoxState
@Composable
private fun rememberSwipeToDismissBoxState(confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = { true }): SimpleSwipeToDismissBoxState = SimpleSwipeToDismissBoxState()
@Composable
private fun SwipeToDismissBox(
    state: SimpleSwipeToDismissBoxState,
    modifier: Modifier = Modifier,
    backgroundContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) { Box(modifier) { content() } }

/**
 * Apple Music Style Mini Player
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleMiniPlayer(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDismiss()
                    true
                }
                else -> false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppleColors.secondary)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Dismiss",
                    tint = Color.White
                )
            }
        },
        content = {
            AppleGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                cornerRadius = 0.dp,
                backgroundColor = AppleColors.background.copy(alpha = 0.98f)
            ) {
                Column {
                    // Progress bar
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .graphicsLayer { translationY = -8f },
                        color = AppleColors.primary,
                        trackColor = AppleColors.textTertiary.copy(alpha = 0.2f)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Album art with glow
                        Box {
                            AsyncImage(
                                model = song.albumArtUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                AppleColors.primary.copy(alpha = 0.5f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Song info
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = song.title,
                                style = AppleTypography.subheadline,
                                color = AppleColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = song.artist,
                                style = AppleTypography.caption1,
                                color = AppleColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Controls
                        Row {
                            IconButton(
                                onClick = onPlayPause,
                                modifier = Modifier.size(40.dp)
                            ) {
                                val scale by animateFloatAsState(
                                    targetValue = if (isPlaying) 1f else 1f,
                                    animationSpec = springBouncy,
                                    label = "play_scale"
                                )
                                
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = AppleColors.textPrimary,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .scale(scale)
                                )
                            }
                            
                            IconButton(
                                onClick = onNext,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Next",
                                    tint = AppleColors.textPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    )
}

/**
 * Apple Music Style Search Bar
 */
@Composable
fun AppleSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onVoiceClick: () -> Unit,
    placeholder: String = "Search",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppleColors.glassWhite)
            .border(
                width = 0.5.dp,
                color = AppleColors.glassBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = AppleColors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            textStyle = AppleTypography.body.copy(color = AppleColors.textPrimary),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = AppleTypography.body,
                            color = AppleColors.textTertiary
                        )
                    }
                    innerTextField()
                }
            }
        )
        
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Clear",
                    tint = AppleColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        IconButton(
            onClick = onVoiceClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Voice search",
                tint = AppleColors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
