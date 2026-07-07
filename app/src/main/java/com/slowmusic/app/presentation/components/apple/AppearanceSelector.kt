package com.slowmusic.app.presentation.components.apple

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.slowmusic.app.presentation.theme.apple.*

/**
 * UI Style Selector Component
 * Allows users to choose between Default and Apple Music styles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UIStyleSelector(
    currentStyle: UIStyle,
    onStyleSelected: (UIStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Choose UI Style",
            style = AppleTypography.headline,
            color = AppleColors.textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UIStyleOption(
                style = UIStyle.DEFAULT,
                isSelected = currentStyle == UIStyle.DEFAULT,
                onClick = { onStyleSelected(UIStyle.DEFAULT) },
                modifier = Modifier.weight(1f)
            )
            
            UIStyleOption(
                style = UIStyle.APPLE_MUSIC,
                isSelected = currentStyle == UIStyle.APPLE_MUSIC,
                onClick = { onStyleSelected(UIStyle.APPLE_MUSIC) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UIStyleOption(
    style: UIStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AppleColors.primary else AppleColors.glassBorder,
        label = "border_color"
    )
    
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        label = "border_width"
    )
    
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                when (style) {
                    UIStyle.DEFAULT -> AppleColors.backgroundLight
                    UIStyle.APPLE_MUSIC -> AppleColors.background
                }
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            // Preview Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (style) {
                            UIStyle.DEFAULT -> AppleColors.primary
                            UIStyle.APPLE_MUSIC -> AppleColors.primary.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (style) {
                        UIStyle.DEFAULT -> Icons.Filled.Home
                        UIStyle.APPLE_MUSIC -> Icons.Filled.MusicNote
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Style Name
            Text(
                text = style.displayName,
                style = AppleTypography.headline,
                color = when (style) {
                    UIStyle.DEFAULT -> AppleColors.textPrimaryDark
                    UIStyle.APPLE_MUSIC -> AppleColors.textPrimary
                },
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Description
            Text(
                text = style.description,
                style = AppleTypography.footnote,
                color = when (style) {
                    UIStyle.DEFAULT -> AppleColors.textSecondaryDark
                    UIStyle.APPLE_MUSIC -> AppleColors.textSecondary
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Features Preview
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                style.features.take(3).forEach { feature ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when (style) {
                                    UIStyle.DEFAULT -> AppleColors.primary.copy(alpha = 0.1f)
                                    UIStyle.APPLE_MUSIC -> AppleColors.glassWhite
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = null,
                            tint = when (style) {
                                UIStyle.DEFAULT -> AppleColors.primary
                                UIStyle.APPLE_MUSIC -> AppleColors.primary
                            },
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            // Selected indicator
            if (isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppleColors.primary)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Active",
                        style = AppleTypography.caption1,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

enum class UIStyle(
    val displayName: String,
    val description: String,
    val features: List<StyleFeature>
) {
    DEFAULT(
        displayName = "Default",
        description = "Material Design 3 with standard animations",
        features = listOf(
            StyleFeature(Icons.Filled.Palette, "Material 3"),
            StyleFeature(Icons.Filled.Animation, "Smooth"),
            StyleFeature(Icons.Filled.CheckCircle, "Standard")
        )
    ),
    APPLE_MUSIC(
        displayName = "Apple Music",
        description = "Glassmorphism, dynamic colors, spring animations",
        features = listOf(
            StyleFeature(Icons.Filled.Layers, "Glass"),
            StyleFeature(Icons.Filled.ColorLens, "Dynamic"),
            StyleFeature(Icons.Filled.Speed, "120Hz")
        )
    )
}

data class StyleFeature(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)

/**
 * Apple Music Style Settings Preview Card
 */
@Composable
fun AppleMusicStylePreviewCard(
    modifier: Modifier = Modifier
) {
    AppleGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(AppleColors.gradientPurple)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Apple Music Style",
                    style = AppleTypography.headline,
                    color = AppleColors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Glassmorphism, dynamic colors, spring animations",
                    style = AppleTypography.footnote,
                    color = AppleColors.textSecondary
                )
            }
            
            AppleToggle(
                checked = true,
                onCheckedChange = { }
            )
        }
    }
}

/**
 * Feature Toggle Row with Apple Music Style
 */
@Composable
fun AppleFeatureToggle(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppleTypography.body,
                color = AppleColors.textPrimary
            )
            
            Text(
                text = subtitle,
                style = AppleTypography.caption1,
                color = AppleColors.textTertiary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        AppleToggle(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
