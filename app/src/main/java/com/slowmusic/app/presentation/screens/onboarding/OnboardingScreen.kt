package com.slowmusic.app.presentation.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.slowmusic.app.presentation.theme.apple.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleColors.background)
    ) {
        // Animated background
        AnimatedBackground()
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onComplete) {
                    Text(
                        text = "Skip",
                        style = AppleTypography.body,
                        color = AppleColors.textSecondary
                    )
                }
            }
            
            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                pageSpacing = 0.dp,
                beyondViewportPageCount = 1
            ) { page ->
                OnboardingPage(
                    page = onboardingPages[page]
                )
            }
            
            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    
                    val size by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "indicator_size"
                    )
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(size, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected) AppleColors.primary
                                else AppleColors.textTertiary.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            
            // Next/Get Started button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
            ) {
                val isLastPage = pagerState.currentPage == 2
                
                Button(
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .graphicsLayer {
                            val scale = if (isLastPage) 1f else 1f
                            scaleX = scale
                            scaleY = scale
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppleColors.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Text(
                        text = if (isLastPage) "Get Started" else "Next",
                        style = AppleTypography.headline,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )
    
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset2"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top right glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppleColors.gradientPurple.first().copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(size.width + offset1, -offset1),
                    radius = 400f
                )
            )
            
            // Bottom left glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppleColors.gradientBlue.first().copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(-offset2, size.height + offset2),
                    radius = 350f
                )
            )
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with animated background
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(page.gradientColors)
                )
                .graphicsLayer {
                    shadowElevation = 24.dp.toPx()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(96.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        
        // Title
        Text(
            text = page.title,
            style = AppleTypography.title1,
            color = AppleColors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            style = AppleTypography.body,
            color = AppleColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Features list
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            page.features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AppleColors.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = AppleColors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = feature,
                        style = AppleTypography.subheadline,
                        color = AppleColors.textPrimary
                    )
                }
            }
        }
    }
}

data class OnboardingPageData(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val features: List<String>,
    val gradientColors: List<Color>
)

val onboardingPages = listOf(
    OnboardingPageData(
        icon = Icons.Filled.MusicNote,
        title = "Welcome to Slow Music",
        description = "Your ultimate music streaming companion. Stream millions of songs or play from your device.",
        features = listOf(
            "Stream from iTunes catalog",
            "Play local music files",
            "Ad-free experience with Premium"
        ),
        gradientColors = AppleColors.gradientGreen
    ),
    OnboardingPageData(
        icon = Icons.Filled.Search,
        title = "Discover New Music",
        description = "Find songs, artists, and albums instantly. Use voice search or browse by genre.",
        features = listOf(
            "Voice search powered",
            "Browse by genre",
            "Trending & recommendations"
        ),
        gradientColors = AppleColors.gradientPurple
    ),
    OnboardingPageData(
        icon = Icons.Filled.Star,
        title = "Make It Yours",
        description = "Create playlists, download for offline, and enjoy premium audio quality.",
        features = listOf(
            "Create playlists",
            "Download for offline",
            "Premium audio quality"
        ),
        gradientColors = AppleColors.gradientPink
    )
)
