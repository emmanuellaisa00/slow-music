package com.slowmusic.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PremiumLockedHeader(
    title: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    trailing: (@Composable () -> Unit)? = null
) {
    val containerAlpha by animateFloatAsState(
        targetValue = if (active) 0.92f else 0.0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 520f),
        label = "locked_header_container_alpha"
    )
    val elevation by animateFloatAsState(
        targetValue = if (active) 10f else 0f,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = 600f),
        label = "locked_header_elevation"
    )
    val bg = if (dark) Color(0xFF101010) else MaterialTheme.colorScheme.background
    val fg = if (dark) Color.White else MaterialTheme.colorScheme.onBackground
    val border = if (dark) Color.White.copy(alpha = 0.10f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bg.copy(alpha = if (active) containerAlpha else 0f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation.dp, RoundedCornerShape(22.dp), clip = false)
                .background(bg.copy(alpha = if (active) containerAlpha else 0f), RoundedCornerShape(22.dp))
                .border(1.dp, border.copy(alpha = if (active) 1f else 0f), RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = fg,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            trailing?.invoke()
        }
    }
}
