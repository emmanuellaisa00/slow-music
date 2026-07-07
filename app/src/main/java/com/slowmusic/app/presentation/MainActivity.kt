package com.slowmusic.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
            val playbackState by mainViewModel.playbackState.collectAsState()
            val currentSong by mainViewModel.currentSong.collectAsState()
            val progress by mainViewModel.progress.collectAsState()
            val repeatMode by mainViewModel.repeatMode.collectAsState()
            val isShuffled by mainViewModel.isShuffled.collectAsState()
            
            SlowMusicTheme(themeMode = themeMode) {
                SlowMusicApp(
                    navigationStyle = navigationStyle,
                    playbackState = playbackState,
                    currentSong = currentSong,
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
    playbackState: com.slowmusic.app.domain.model.PlaybackState,
    currentSong: com.slowmusic.app.domain.model.Song?,
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
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            when (navigationStyle) {
                com.slowmusic.app.domain.model.NavigationStyle.TABS,
                com.slowmusic.app.domain.model.NavigationStyle.BOTTOM_NAV -> {
                    Column {
                        if (showMiniPlayer) {
                            MiniPlayer(
                                song = currentSong!!,
                                isPlaying = playbackState == com.slowmusic.app.domain.model.PlaybackState.PLAYING,
                                onPlayPause = onPlayPause,
                                onNext = onNext,
                                onClick = { navController.navigate(Screen.Player.route) }
                            )
                        }
                        NavigationBar {
                            bottomNavItems.forEach { item ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == item.screen.route) 
                                                item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    },
                                    label = { Text(item.title) },
                                    selected = currentRoute == item.screen.route,
                                    onClick = {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(Screen.Home.route) {
                                                saveState = true
                                            }
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
    ) { paddingValues ->
        NavigationGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
            playbackState = playbackState,
            currentSong = currentSong,
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
            onAddToQueue = onAddToQueue
        )
    }
}
