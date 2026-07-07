package com.slowmusic.app.presentation.components.apple

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.slowmusic.app.presentation.theme.apple.*

/**
 * Apple Music-inspired Glassmorphism Card
 * Features subtle frosted glass effect with soft blur
 */
@Composable
fun AppleGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    backgroundColor: Color = AppleColors.glassWhite,
    borderColor: Color = AppleColors.glassBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

/**
 * Liquid Glass Effect - Soft gradient overlay
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        AppleColors.glassWhite,
        AppleColors.glassBlack
    ),
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.linearGradient(gradientColors)
            )
            .border(
                width = 0.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

/**
 * Apple-style Settings Row with liquid glass effect
 */
@Composable
fun AppleSettingsRow(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val alpha = if (enabled) 1f else 0.5f
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, onClick = onClick)
                } else {
                    Modifier
                }
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }
            
            // Title & Subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = AppleTypography.headline,
                    color = AppleColors.textPrimary.copy(alpha = alpha)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = AppleTypography.footnote,
                        color = AppleColors.textSecondary.copy(alpha = alpha)
                    )
                }
            }
            
            // Value or Trailing
            if (value != null) {
                Text(
                    text = value,
                    style = AppleTypography.body,
                    color = AppleColors.textTertiary
                )
            }
            
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Image(
                    imageVector = androidx.compose.material.icons.Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { this.alpha = alpha },
                    colorFilter = ColorFilter.tint(AppleColors.textTertiary)
                )
            }
        }
    }
}

/**
 * Apple-style Section Header
 */
@Composable
fun AppleSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = AppleTypography.footnote,
        fontWeight = FontWeight.SemiBold,
        color = AppleColors.textSecondary,
        modifier = modifier.padding(start = 32.dp, top = 32.dp, bottom = 8.dp)
    )
}

/**
 * Apple-style Toggle with smooth animation
 */
@Composable
fun AppleToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val toggleAlpha = if (enabled) 1f else 0.5f
    
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumb_offset"
    )
    
    Box(
        modifier = modifier
            .width(51.dp)
            .height(31.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (checked) AppleColors.primary 
                else AppleColors.textTertiary.copy(alpha = 0.3f)
            )
            .graphicsLayer { this.alpha = toggleAlpha }
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = 2.dp + thumbOffset)
                .size(27.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * Apple-style Slider with spring animation
 */
@Composable
fun AppleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true
) {
    val sliderAlpha = if (enabled) 1f else 0.5f
    
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "slider_value"
    )
    
    Column(modifier = modifier) {
        Slider(
            value = animatedValue,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { this.alpha = sliderAlpha },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = AppleColors.primary,
                inactiveTrackColor = AppleColors.textTertiary.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * Apple-style Segmented Control
 */
@Composable
fun <T> AppleSegmentedControl(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    val selectedIndex = options.indexOf(selectedOption)
    val indicatorOffset by animateFloatAsState(
        targetValue = selectedIndex.toFloat() / options.size,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "segment_indicator"
    )
    
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppleColors.glassWhite)
            .border(
                width = 1.dp,
                color = AppleColors.glassBorder,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Animated indicator
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(1f / options.size)
                .padding(2.dp)
                .offset(x = (indicatorOffset * 100).dp * (options.size - 1) / options.size)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.2f))
        )
        
        // Options
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onOptionSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelProvider(option),
                        style = AppleTypography.subheadline,
                        fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedIndex == index) 
                            AppleColors.textPrimary 
                        else 
                            AppleColors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Apple-style Navigation Bar (for Settings)
 */
@Composable
fun AppleNavigationBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 0.dp,
        gradientColors = listOf(
            AppleColors.glassWhite,
            AppleColors.glassBlack
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Image(
                        imageVector = androidx.compose.material.icons.Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(AppleColors.primary)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            // Title
            Text(
                text = title,
                style = AppleTypography.headline,
                color = AppleColors.textPrimary
            )
            
            // Trailing
            if (trailing != null) {
                trailing()
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

/**
 * Apple-style Floating Action Button
 */
@Composable
fun AppleFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = AppleSpringAnimations.springBouncy,
        label = "fab_scale"
    )
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        containerColor = AppleColors.primary,
        contentColor = Color.White,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        content()
    }
}

/**
 * Apple-style Bottom Sheet with glassmorphism
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleBottomSheet(
    sheetState: SheetState,
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = AppleColors.background,
        contentColor = AppleColors.textPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(AppleColors.textTertiary)
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding(),
                content = sheetContent
            )
        }
    )
}

/**
 * Apple-style Progress Indicator
 */
@Composable
fun AppleProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = AppleColors.primary,
    trackColor: Color = AppleColors.textTertiary.copy(alpha = 0.3f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "progress"
    )
    
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

/**
 * Apple-style Loading Spinner
 */
@Composable
fun AppleLoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = AppleColors.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotation
                },
            color = color,
            strokeWidth = 3.dp
        )
    }
}

/**
 * Animated List Item with stagger effect
 */
@Composable
fun AnimatedListItem(
    visible: Boolean,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "item_alpha"
    )
    
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "item_offset"
    )
    
    Box(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY
            }
    ) {
        content()
    }
}
