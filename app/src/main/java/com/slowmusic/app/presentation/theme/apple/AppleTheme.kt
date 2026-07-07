package com.slowmusic.app.presentation.theme.apple

import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.*

/**
 * Apple Music-inspired Theme Configuration
 * Features:
 * - Glassmorphism effects
 * - Dynamic colors from album art
 * - Spring animations
 * - Large rounded corners
 * - SF Pro-style typography (Inter)
 */
data class AppleMusicThemeConfig(
    val enableGlassmorphism: Boolean = true,
    val enableLiquidGlass: Boolean = true,
    val enableDynamicColors: Boolean = true,
    val enableSpringAnimations: Boolean = true,
    val cornerRadius: CornerRadiusStyle = CornerRadiusStyle.LARGE,
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL
)

enum class CornerRadiusStyle(val dp: Dp) {
    MEDIUM(16.dp),
    LARGE(24.dp),
    EXTRA_LARGE(32.dp)
}

enum class AnimationSpeed(val duration: Int) {
    FAST(150),
    NORMAL(300),
    SLOW(500)
}

// Dynamic Color Extraction from album art
object DynamicColorExtractor {
    private val vibrantColors = mutableStateListOf<Color>()
    private val mutedColors = mutableStateListOf<Color>()
    private val darkVibrantColors = mutableStateListOf<Color>()
    private val darkMutedColors = mutableStateListOf<Color>()

    fun extractColors(bitmap: androidx.compose.ui.graphics.ImageBitmap) {
        // Simplified color extraction - in production, use AndroidX Palette
        vibrantColors.clear()
        vibrantColors.addAll(listOf(
            Color(0xFF1DB954),
            Color(0xFF169C46),
            Color(0xFF1ED760)
        ))
    }

    @Composable
    fun getPrimaryColor(): Color {
        return if (vibrantColors.isNotEmpty()) {
            vibrantColors.first()
        } else {
            AppleColors.primary
        }
    }

    @Composable
    fun getSecondaryColor(): Color {
        return if (darkVibrantColors.isNotEmpty()) {
            darkVibrantColors.first()
        } else {
            AppleColors.secondary
        }
    }

    @Composable
    fun getAccentColor(): Color {
        return if (vibrantColors.size > 1) {
            vibrantColors[1]
        } else {
            AppleColors.accent
        }
    }

    @Composable
    fun getGradientColors(): List<Color> {
        return if (vibrantColors.isNotEmpty()) {
            listOf(vibrantColors.first(), AppleColors.background)
        } else {
            AppleColors.defaultGradient
        }
    }
}

// Apple-style Colors
object AppleColors {
    val primary = Color(0xFF1DB954)
    val primaryDark = Color(0xFF169C46)
    val primaryLight = Color(0xFF1ED760)
    
    val secondary = Color(0xFFFE2952)
    val tertiary = Color(0xFFFF375F)
    
    // Background colors
    val background = Color(0xFF000000)
    val backgroundLight = Color(0xFFF5F5F7)
    val surfaceGlass = Color(0x1AFFFFFF)
    val surfaceGlassLight = Color(0x1A000000)
    
    // Text colors
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0x99FFFFFF)
    val textTertiary = Color(0x66FFFFFF)
    
    val textPrimaryDark = Color(0xFF1D1D1F)
    val textSecondaryDark = Color(0xFF86868B)
    
    // Accent gradients
    val gradientPink = listOf(Color(0xFFFF2D55), Color(0xFFFF6B6B))
    val gradientPurple = listOf(Color(0xFFAF52DE), Color(0xFF5856D6))
    val gradientBlue = listOf(Color(0xFF007AFF), Color(0xFF5AC8FA))
    val gradientOrange = listOf(Color(0xFFFF9500), Color(0xFFFFCC00))
    val gradientGreen = listOf(Color(0xFF34C759), Color(0xFF30D158))
    
    val defaultGradient = listOf(primary, primaryLight)
    
    // Glass effect colors
    val glassWhite = Color(0x1AFFFFFF)
    val glassBlack = Color(0x0DFFFFFF)
    val glassBorder = Color(0x33FFFFFF)
    val glassBorderLight = Color(0x1A000000)
}

// Typography with Inter font
object AppleTypography {
    val largeTitle = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 41.sp,
        letterSpacing = 0.37.sp
    )
    
    val title1 = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.36.sp
    )
    
    val title2 = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.35.sp
    )
    
    val title3 = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.38.sp
    )
    
    val headline = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.41).sp
    )
    
    val body = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Regular,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.41).sp
    )
    
    val callout = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Regular,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.32).sp
    )
    
    val subheadline = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Regular,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.24).sp
    )
    
    val footnote = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Regular,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.08).sp
    )
    
    val caption1 = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Regular,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
    
    val caption2 = TextStyle(
        fontFamily = FontFamily.Inter,
        fontWeight = FontWeight.Regular,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.07.sp
    )
}

// Spring Animation Presets
object AppleSpringAnimations {
    val spring1 = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessMedium
    )
    
    val spring2 = spring<Float>(
        dampingRatio = 0.7f,
        stiffness = Spring.StiffnessMediumLow
    )
    
    val springBouncy = spring<Float>(
        dampingRatio = 0.5f,
        stiffness = Spring.StiffnessMedium
    )
    
    val springGentle = spring<Float>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessLow
    )
}

// Smooth Animations
@Composable
fun animateAsStateAppleMusic(
    targetValue: Float,
    animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    key: Any? = null
): State<Float> {
    val animation = tween<Float>(
        durationMillis = animationSpeed.duration,
        easing = FastOutSlowInEasing
    )
    
    return animateFloatAsState(
        targetValue = targetValue,
        animationSpec = animation,
        key = key,
        label = "apple_animation"
    )
}

@Composable
fun animateScaleApple(
    visible: Boolean,
    animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL
): Float {
    return animateAsStateAppleMusic(
        targetValue = if (visible) 1f else 0f,
        animationSpeed = animationSpeed
    ).value
}
