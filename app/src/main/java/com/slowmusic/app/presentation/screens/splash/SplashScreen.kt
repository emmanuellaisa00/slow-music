package com.slowmusic.app.presentation.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slowmusic.app.R
import com.slowmusic.app.presentation.theme.apple.AppleTypography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    showOnboarding: Boolean = false
) {
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
        label = "premium_logo_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(260),
        label = "premium_splash_alpha"
    )

    LaunchedEffect(Unit) {
        delay(360)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF07120C),
                        Color(0xFF050706),
                        Color.Black
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer { this.alpha = alpha }
        ) {
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .scale(logoScale)
                    .clip(RoundedCornerShape(34.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1ED760), Color(0xFF0FA84B), Color(0xFF051F12))
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.50f), Color.White.copy(alpha = 0.05f))),
                        shape = RoundedCornerShape(34.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Slow Music",
                    modifier = Modifier.size(92.dp)
                )
            }

            Spacer(Modifier.height(26.dp))
            Text(
                text = "Slow Music",
                style = AppleTypography.largeTitle,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "from LAISER ORG",
                style = AppleTypography.caption1,
                color = Color.White.copy(alpha = 0.58f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
