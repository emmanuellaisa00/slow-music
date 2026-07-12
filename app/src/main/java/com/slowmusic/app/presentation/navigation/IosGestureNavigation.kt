package com.slowmusic.app.presentation.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private enum class IosDragMode { BackPush, DismissModal }

/**
 * Adds iPhone-like interactive gestures across the app shell:
 *
 * - Push/detail pages: swipe from the left edge to go back.
 * - Modal pages: pull down from the top area to dismiss.
 * - Root tabs keep the regular tab interaction and do not hijack horizontal
 *   scrolling inside lists/carousels.
 */
@Composable
fun IosGestureNavigationLayer(
    enabled: Boolean,
    currentRoute: String?,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val edgeWidthPx = with(density) { 34.dp.toPx() }
    val topGrabPx = with(density) { 132.dp.toPx() }
    val thresholdPx = with(density) { 92.dp.toPx() }
    val maxHorizontalPx = with(density) { 220.dp.toPx() }
    val maxVerticalPx = with(density) { 260.dp.toPx() }

    var dragMode by remember(currentRoute) { mutableStateOf<IosDragMode?>(null) }
    var thresholdHapticFired by remember(currentRoute) { mutableStateOf(false) }
    var rawX by remember(currentRoute) { mutableStateOf(0f) }
    var rawY by remember(currentRoute) { mutableStateOf(0f) }
    val shownX by animateFloatAsState(
        targetValue = rawX,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = 560f),
        label = "ios_nav_swipe_x"
    )
    val shownY by animateFloatAsState(
        targetValue = rawY,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = 560f),
        label = "ios_nav_swipe_y"
    )

    val route = currentRoute
    val isModal = route.isModalRoute()
    val allowsPushSwipe = route.isDetailPushRoute() || route.isLibraryChildRoute()
    val allowsModalDismiss = isModal
    val gesturesEnabled = enabled && canNavigateBack && route != null && !route.isRootTabRoute()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(gesturesEnabled, route) {
                if (!gesturesEnabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { start: Offset ->
                        rawX = 0f
                        rawY = 0f
                        thresholdHapticFired = false
                        dragMode = when {
                            allowsPushSwipe && start.x <= edgeWidthPx -> IosDragMode.BackPush
                            allowsModalDismiss && start.y <= topGrabPx -> IosDragMode.DismissModal
                            else -> null
                        }
                    },
                    onDrag = { change, dragAmount ->
                        when (dragMode) {
                            IosDragMode.BackPush -> {
                                // Only claim rightward, mostly-horizontal drags.
                                if (dragAmount.x > 0f && abs(dragAmount.x) >= abs(dragAmount.y) * 0.55f) {
                                    rawX = (rawX + dragAmount.x).coerceIn(0f, maxHorizontalPx)
                                    if (!thresholdHapticFired && rawX >= thresholdPx) {
                                        thresholdHapticFired = true
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            }
                            IosDragMode.DismissModal -> {
                                // Only claim downward, mostly-vertical drags.
                                if (dragAmount.y > 0f && abs(dragAmount.y) >= abs(dragAmount.x) * 0.55f) {
                                    rawY = (rawY + dragAmount.y).coerceIn(0f, maxVerticalPx)
                                    if (!thresholdHapticFired && rawY >= thresholdPx) {
                                        thresholdHapticFired = true
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            }
                            null -> Unit
                        }
                    },
                    onDragCancel = {
                        dragMode = null
                        thresholdHapticFired = false
                        rawX = 0f
                        rawY = 0f
                    },
                    onDragEnd = {
                        val shouldNavigateBack = when (dragMode) {
                            IosDragMode.BackPush -> rawX >= thresholdPx
                            IosDragMode.DismissModal -> rawY >= thresholdPx
                            null -> false
                        }
                        dragMode = null
                        thresholdHapticFired = false
                        rawX = 0f
                        rawY = 0f
                        if (shouldNavigateBack) onNavigateBack()
                    }
                )
            }
            .graphicsLayer {
                translationX = shownX
                translationY = shownY
                if (shownY > 0f) {
                    val modalScale = 1f - (shownY / maxVerticalPx * 0.035f).coerceIn(0f, 0.035f)
                    scaleX = modalScale
                    scaleY = modalScale
                    alpha = 1f - (shownY / maxVerticalPx * 0.08f).coerceIn(0f, 0.08f)
                }
            }
    ) {
        content()
    }
}
