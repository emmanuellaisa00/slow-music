package com.slowmusic.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.slowmusic.app.domain.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = OnPrimary,
    secondary = AccentPurple,
    onSecondary = OnSecondary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = AccentPink,
    onTertiary = OnPrimary,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = TextPrimaryDark,
    error = Error,
    onError = OnError,
    errorContainer = Error.copy(alpha = 0.3f),
    onErrorContainer = OnError,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = TextSecondaryDark,
    outlineVariant = DarkSurfaceElevated,
    inverseSurface = LightSurface,
    inverseOnSurface = TextPrimaryLight,
    inversePrimary = PrimaryGreenDark,
    surfaceTint = PrimaryGreen
)

private val AppleGlassColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = OnPrimary,
    secondary = AccentPink,
    onSecondary = OnPrimary,
    tertiary = AccentPurple,
    background = Color(0xFF050507),
    onBackground = TextPrimaryDark,
    surface = Color(0x22FFFFFF),
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0x18FFFFFF),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0x33FFFFFF),
    outlineVariant = Color(0x22FFFFFF),
    error = Error,
    onError = OnError,
    surfaceTint = PrimaryGreen
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryGreenLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = AccentPurple,
    onSecondary = OnPrimary,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = AccentPink,
    onTertiary = OnPrimary,
    tertiaryContainer = LightSurfaceVariant,
    onTertiaryContainer = TextPrimaryLight,
    error = Error,
    onError = OnError,
    errorContainer = Error.copy(alpha = 0.3f),
    onErrorContainer = OnError,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = TextSecondaryLight,
    outlineVariant = LightSurfaceElevated,
    inverseSurface = DarkSurface,
    inverseOnSurface = TextPrimaryDark,
    inversePrimary = PrimaryGreenLight,
    surfaceTint = PrimaryGreen
)

@Composable
fun SlowMusicTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    useAppleMusicUi: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useAppleMusicUi) AppleGlassColorScheme else if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme && !useAppleMusicUi
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme && !useAppleMusicUi
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
