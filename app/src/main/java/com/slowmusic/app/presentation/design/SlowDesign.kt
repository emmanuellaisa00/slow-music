package com.slowmusic.app.presentation.design

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.dp

object SlowSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

object SlowRadius {
    val sm = 10.dp
    val md = 14.dp
    val lg = 18.dp
    val xl = 24.dp
    val xxl = 28.dp
    val pill = 999.dp
}

object SlowElevation {
    val none = 0.dp
    val subtle = 1.dp
    val card = 3.dp
    val raised = 8.dp
    val floating = 12.dp
}

object SlowSize {
    val minTouch = 48.dp
    val icon = 24.dp
    val artworkSmall = 52.dp
    val miniArtwork = 46.dp
    val cardWidth = 160.dp
    val cardArtworkRadius = SlowRadius.md
}

object SlowMotion {
    const val quick = 160
    const val standard = 220
    const val emphasized = 320
    val easeOut = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasizedEase = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
