package com.slowmusic.app.presentation.screens.splash

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.slowmusic.app.R
import com.slowmusic.app.presentation.theme.apple.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    showOnboarding: Boolean = false
) {
    // Animation states
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500),
        label = "logo_alpha"
    )
    
    val textAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, delayMillis = 300),
        label = "text_alpha"
    )
    
    LaunchedEffect(Unit) {
        delay(650)
        if (showOnboarding) {
            onNavigateToOnboarding()
        } else {
            onNavigateToHome()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleColors.background),
        contentAlignment = Alignment.Center
    ) {
        // Animated gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.3f }
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "bg")
            val offset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(10000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bg_offset"
            )
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppleColors.primary.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.3f, size.height * 0.3f),
                        radius = 800f + offset
                    )
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with animation
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .graphicsLayer { alpha = logoAlpha }
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.linearGradient(AppleColors.gradientGreen)
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Slow Music logo",
                    modifier = Modifier.size(86.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App name
            Text(
                text = "Slow Music",
                style = AppleTypography.largeTitle,
                color = AppleColors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "Online, local, and cached beautifully",
                style = AppleTypography.body,
                color = AppleColors.textSecondary,
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AppleColors.primary,
                strokeWidth = 2.dp
            )
        }
    }
}
