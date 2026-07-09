package com.slowmusic.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.slowmusic.app.presentation.components.MiniPlayer
import com.slowmusic.app.presentation.components.apple.AppleMiniPlayer
import com.slowmusic.app.presentation.navigation.*
import com.slowmusic.app.presentation.theme.SlowMusicTheme
import com.slowmusic.app.streaming.WebViewStreamResolver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var cleanupWebViewResolver: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cleanupWebViewResolver = WebViewStreamResolver.init(this)

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val themeMode by mainViewModel.themeMode.collectAsState()
            val navigationStyle by mainViewModel.navigationStyle.collectAsState()
            val userPreferences by mainViewModel.userPreferences.collectAsState()
            val useIosGlass = userPreferences.uiStyle == com.slowmusic.app.domain.model.UIStyle.IOS_GLASS
            val useAppleMusicUi = userPreferences.uiStyle == com.slowmusic.app.domain.model.UIStyle.APPLE_MUSIC || useIosGlass
            val playbackState by mainViewModel.playbackState.collectAsState()
            val currentSong by mainViewModel.currentSong.collectAsState()
            val queue by mainViewModel.queue.collectAsState()
            val lyrics by mainViewModel.lyrics.collectAsState()
            val progress by mainViewModel.progress.collectAsState()
            val repeatMode by mainViewModel.repeatMode.collectAsState()
            val isShuffled by mainViewModel.isShuffled.collectAsState()

            SlowMusicTheme(themeMode = themeMode, useAppleMusicUi = useAppleMusicUi) {
                SlowMusicApp(
                    navigationStyle = navigationStyle,
                    useAppleMusicUi = useAppleMusicUi,
                    useIosGlass = useIosGlass,
                    playbackState = playbackState,
                    currentSong = currentSong,
                    queue = queue,
                    lyrics = lyrics,
                    onPlayPause = { mainViewModel.togglePlayPause() },
                    onNext = { mainViewModel.playNext() },
                    onPrevious = { mainViewModel.playPrevious() },
                    onPlaySong = { song, queue -> mainViewModel.playSong(song, queue) },
                    progress = progress,
                    repeatMode = repeatMode,
                    isShuffled = isShuffled,
                    onSeek = { mainViewModel.seekTo(it) },
                    onToggleShuffle = { mainViewModel.toggleShuffle() },
                    onToggleRepeat = { mainViewModel.toggleRepeat() },
                    onToggleFavorite = { mainViewModel.toggleFavorite() },
                    onDownload = { song -> mainViewModel.downloadSong(song) },
                    onAddToQueue = { song -> mainViewModel.addToQueue(song) },
                    onRemoveFromQueue = { index -> mainViewModel.removeFromQueue(index) },
                    onMoveQueueItem = { from, to -> mainViewModel.moveQueueItem(from, to) },
                    onSaveQueueAsPlaylist = { mainViewModel.saveQueueAsPlaylist() },
                    onClearQueue = { mainViewModel.clearQueue() },
                    onMiniPlayerClick = { }
                )
            }
        }
    }

    override fun onDestroy() {
        cleanupWebViewResolver?.invoke()
        cleanupWebViewResolver = null
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlowMusicApp(
    navigationStyle: com.slowmusic.app.domain.model.NavigationStyle,
    useAppleMusicUi: Boolean,
    useIosGlass: Boolean,
    playbackState: com.slowmusic.app.domain.model.PlaybackState,
    currentSong: com.slowmusic.app.domain.model.Song?,
    queue: List<com.slowmusic.app.domain.model.Song>,
    lyrics: String?,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlaySong: (com.slowmusic.app.domain.model.Song, List<com.slowmusic.app.domain.model.Song>) -> Unit,
    progress: Float,
    repeatMode: com.slowmusic.app.domain.model.RepeatMode,
    isShuffled: Boolean,
    onSeek: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: (com.slowmusic.app.domain.model.Song) -> Unit,
    onAddToQueue: (com.slowmusic.app.domain.model.Song) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onSaveQueueAsPlaylist: () -> Unit,
    onClearQueue: () -> Unit,
    onMiniPlayerClick: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Library.route,
        Screen.Profile.route
    )

    val showMiniPlayer = currentSong != null && currentRoute != Screen.Player.route

    Box(modifier = Modifier.fillMaxSize()) {
        if (useAppleMusicUi) AppleGlassAppBackground()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = if (useAppleMusicUi) Color.Transparent else MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                when (navigationStyle) {
                    com.slowmusic.app.domain.model.NavigationStyle.TABS,
                    com.slowmusic.app.domain.model.NavigationStyle.BOTTOM_NAV -> {
                        Column {
                            if (showMiniPlayer) {
                                if (useAppleMusicUi) {
                                    AppleMiniPlayer(
                                        song = currentSong!!,
                                        isPlaying = playbackState == com.slowmusic.app.domain.model.PlaybackState.PLAYING,
                                        progress = progress,
                                        onPlayPause = onPlayPause,
                                        onNext = onNext,
                                        onClick = { navController.navigate(Screen.Player.route) },
                                        onDismiss = { }
                                    )
                                } else {
                                    MiniPlayer(
                                        song = currentSong!!,
                                        isPlaying = playbackState == com.slowmusic.app.domain.model.PlaybackState.PLAYING,
                                        onPlayPause = onPlayPause,
                                        onNext = onNext,
                                        onClick = { navController.navigate(Screen.Player.route) },
                                        progress = progress
                                    )
                                }
                            }
                            NavigationBar(
                                containerColor = if (useAppleMusicUi) Color.White.copy(alpha = 0.08f) else NavigationBarDefaults.containerColor,
                                tonalElevation = if (useAppleMusicUi) 0.dp else NavigationBarDefaults.Elevation,
                                modifier = if (useAppleMusicUi) Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(28.dp))
                                else Modifier
                            ) {
                                bottomNavItems.forEach { item ->
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == item.screen.route) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.title
                                            )
                                        },
                                        label = { Text(item.title) },
                                        selected = currentRoute == item.screen.route,
                                        onClick = {
                                            navController.navigate(item.screen.route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    com.slowmusic.app.domain.model.NavigationStyle.DRAWER -> {
                        if (showMiniPlayer) {
                            if (useAppleMusicUi) {
                                AppleMiniPlayer(
                                    song = currentSong!!,
                                    isPlaying = playbackState == com.slowmusic.app.domain.model.PlaybackState.PLAYING,
                                    progress = progress,
                                    onPlayPause = onPlayPause,
                                    onNext = onNext,
                                    onClick = { navController.navigate(Screen.Player.route) },
                                    onDismiss = { }
                                )
                            } else {
                                MiniPlayer(
                                    song = currentSong!!,
                                    isPlaying = playbackState == com.slowmusic.app.domain.model.PlaybackState.PLAYING,
                                    onPlayPause = onPlayPause,
                                    onNext = onNext,
                                    onClick = { navController.navigate(Screen.Player.route) }
                                )
                            }
                        }
                    }
                }
                }
            }
        ) { paddingValues ->
            NavigationGraph(
                navController = navController,
                modifier = Modifier.padding(paddingValues),
                playbackState = playbackState,
                currentSong = currentSong,
                useAppleMusicUi = useAppleMusicUi,
                useIosGlass = useIosGlass,
                queue = queue,
                lyrics = lyrics,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onPlaySong = onPlaySong,
                progress = progress,
                repeatMode = repeatMode,
                isShuffled = isShuffled,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat,
                onToggleFavorite = onToggleFavorite,
                onDownload = onDownload,
                onAddToQueue = onAddToQueue,
                onRemoveFromQueue = onRemoveFromQueue,
                onMoveQueueItem = onMoveQueueItem,
                onSaveQueueAsPlaylist = onSaveQueueAsPlaylist,
                onClearQueue = onClearQueue
            )
        }
    }
}

@Composable
private fun AppleGlassAppBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF101014),
                        Color(0xFF050507),
                        Color.Black
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(340.dp)
                .offset(x = (-80).dp, y = 20.dp)
                .blur(80.dp)
                .background(Color(0xFF1DB954).copy(alpha = 0.34f), RoundedCornerShape(180.dp))
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .offset(x = 90.dp, y = 80.dp)
                .blur(90.dp)
                .background(Color(0xFFFF2D55).copy(alpha = 0.22f), RoundedCornerShape(160.dp))
        )
    }
}
