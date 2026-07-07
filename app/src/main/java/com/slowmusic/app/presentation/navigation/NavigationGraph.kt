package com.slowmusic.app.presentation.navigation

import android.Manifest
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.slowmusic.app.domain.model.PlaybackState
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.presentation.screens.home.HomeScreen
import com.slowmusic.app.presentation.screens.library.*
import com.slowmusic.app.presentation.screens.profile.ProfileScreen
import com.slowmusic.app.presentation.screens.search.SearchScreen
import com.slowmusic.app.presentation.screens.splash.SplashScreen
import com.slowmusic.app.presentation.screens.onboarding.OnboardingScreen
import com.slowmusic.app.presentation.screens.settings.LogsScreen
import com.slowmusic.app.presentation.screens.settings.SettingsScreen
import com.slowmusic.app.presentation.screens.settings.EqualizerControlScreen
import com.slowmusic.app.presentation.screens.player.AppleMusicPlayerScreen
import com.slowmusic.app.presentation.screens.player.QueueScreen
import com.slowmusic.app.presentation.screens.player.LyricsScreen
import com.slowmusic.app.presentation.screens.details.*

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    playbackState: PlaybackState,
    currentSong: Song?,
    queue: List<Song>,
    lyrics: String?,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    progress: Float,
    repeatMode: com.slowmusic.app.domain.model.RepeatMode,
    isShuffled: Boolean,
    onSeek: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onSaveQueueAsPlaylist: () -> Unit,
    onClearQueue: () -> Unit
) {

    fun sameSong(a: Song?, b: Song): Boolean {
        if (a == null) return false
        if (a.id == b.id) return true
        val durationClose = a.duration <= 0 || b.duration <= 0 || kotlin.math.abs(a.duration - b.duration) <= 3000
        return a.title.equals(b.title, ignoreCase = true) && a.artist.equals(b.artist, ignoreCase = true) && durationClose
    }

    fun selectSong(song: Song, queue: List<Song>) {
        if (sameSong(currentSong, song)) {
            navController.navigate(Screen.Player.route) { launchSingleTop = true }
        } else {
            onPlaySong(song, queue.ifEmpty { listOf(song) })
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            val context = LocalContext.current
            val prefs = context.getSharedPreferences("slow_music_onboarding", Context.MODE_PRIVATE)
            val hasCompleted = prefs.getBoolean("completed", false)
            SplashScreen(
                showOnboarding = !hasCompleted,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            val context = LocalContext.current
            OnboardingScreen(
                onComplete = {
                    context.getSharedPreferences("slow_music_onboarding", Context.MODE_PRIVATE)
                        .edit().putBoolean("completed", true).apply()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Tabs
        composable(Screen.Home.route) {
            HomeScreen(
                onSongClick = { song, queue ->
                    selectSong(song, queue)
                },
                onArtistClick = { artistId ->
                    navController.navigate(Screen.ArtistDetails.createRoute(artistId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.AlbumDetails.createRoute(albumId))
                },
                onGenreClick = { genreId ->
                    navController.navigate(Screen.GenreDetails.createRoute(genreId))
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToSeeAll = { section ->
                    navController.navigate(Screen.Search.route)
                },
                onAddToPlaylist = { song -> navController.navigate(Screen.AddToPlaylist.createRoute(song)) },
                onAddToQueue = { song -> onAddToQueue(song) },
                onDownload = { song -> onDownload(song) },
                onShare = { }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onSongClick = { song, queue ->
                    selectSong(song, queue)
                },
                onArtistClick = { artistId ->
                    navController.navigate(Screen.ArtistDetails.createRoute(artistId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.AlbumDetails.createRoute(albumId))
                },
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetails.createRoute(playlistId))
                },
                onGenreClick = { genreId ->
                    navController.navigate(Screen.GenreDetails.createRoute(genreId))
                },
                onAddToPlaylist = { song -> navController.navigate(Screen.AddToPlaylist.createRoute(song)) },
                onAddToQueue = { song -> onAddToQueue(song) },
                onDownload = { song -> onDownload(song) },
                onShare = { }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onSongClick = { song, queue ->
                    selectSong(song, queue)
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                onNavigateToRecent = {
                    navController.navigate(Screen.RecentPlays.route)
                },
                onNavigateToMostPlayed = {
                    navController.navigate(Screen.MostPlayed.route)
                },
                onNavigateToDownloads = {
                    navController.navigate(Screen.Downloads.route)
                },
                onNavigateToLocalMusic = {
                    navController.navigate(Screen.LocalMusic.route)
                },
                onNavigateToPlaylists = {
                    navController.navigate(Screen.Playlists.route)
                },
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetails.createRoute(playlistId))
                },
                onNavigateToArtists = {
                    navController.navigate(Screen.Artists.route)
                },
                onNavigateToAlbums = {
                    navController.navigate(Screen.Albums.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onAddToPlaylist = { song -> navController.navigate(Screen.AddToPlaylist.createRoute(song)) },
                onAddToQueue = { song -> onAddToQueue(song) },
                onDownload = { song -> onDownload(song) },
                onShare = { }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToSubscription = {
                    navController.navigate(Screen.Subscription.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Library Sub-screens
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onSongClick = { song, queue ->
                    selectSong(song, queue)
                }
            )
        }

        composable(Screen.RecentPlays.route) {
            RecentPlaysScreen(
                onSongClick = { song, queue ->
                    selectSong(song, queue)
                }
            )
        }

        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onSongClick = { song, queue ->
                    selectSong(song, queue)
                }
            )
        }


        composable(Screen.MostPlayed.route) { MostPlayedScreen(onSongClick = { song, queue -> selectSong(song, queue) }) }

        composable(Screen.LocalMusic.route) { LocalMusicScreen(onSongClick = { song, queue -> selectSong(song, queue) }) }

        composable(Screen.Playlists.route) { PlaylistsScreen(onPlaylistClick = { navController.navigate(Screen.PlaylistDetails.createRoute(it)) }) }

        composable(Screen.Artists.route) { FollowedArtistsScreen(onArtistClick = { navController.navigate(Screen.ArtistDetails.createRoute(it)) }) }

        composable(Screen.Albums.route) { SavedAlbumsScreen(onAlbumClick = { navController.navigate(Screen.AlbumDetails.createRoute(it)) }) }


        composable(Screen.Player.route) {
            val song = currentSong
            if (song == null) {
                LegalTextScreen(
                    title = "Now Playing",
                    body = "Choose a song from Home, Search, Library, Artist, or Album to start playback.",
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                AppleMusicPlayerScreen(
                    song = song,
                    isPlaying = playbackState == PlaybackState.PLAYING,
                    progress = progress,
                    repeatMode = repeatMode,
                    isShuffled = isShuffled,
                    isFavorite = false,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeek = onSeek,
                    onToggleFavorite = onToggleFavorite,
                    onToggleShuffle = onToggleShuffle,
                    onToggleRepeat = onToggleRepeat,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLyrics = { navController.navigate(Screen.Lyrics.route) },
                    onNavigateToQueue = { navController.navigate(Screen.Queue.route) },
                    onNavigateToCast = { navController.navigate(Screen.CastDevices.route) },
                    onMoreOptions = { navController.navigate(Screen.AddToPlaylist.createRoute(song)) },
                    onShare = { }
                )
            }
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogs = { navController.navigate(Screen.Logs.route) },
                onNavigateToStorage = { navController.navigate(Screen.DownloadStorage.route) },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onNavigateToTerms = { navController.navigate(Screen.Terms.route) },
                onNavigateToNotifications = { navController.navigate(Screen.NotificationPermission.route) },
                onNavigateToLocalFilesPermission = { navController.navigate(Screen.LocalFilesPermission.route) },
                onNavigateToCastDevices = { navController.navigate(Screen.CastDevices.route) },
                onNavigateToEqualizer = { navController.navigate(Screen.Equalizer.route) }
            )
        }


        composable(Screen.Equalizer.route) {
            EqualizerControlScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Logs.route) {
            LogsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }


        composable(Screen.Queue.route) {
            QueueScreen(
                currentSong = currentSong,
                queue = queue,
                onSongClick = { song -> selectSong(song, queue) },
                onRemoveFromQueue = onRemoveFromQueue,
                onMoveQueueItem = onMoveQueueItem,
                onClearQueue = onClearQueue,
                onSaveAsPlaylist = onSaveQueueAsPlaylist,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Lyrics.route) {
            currentSong?.let { song ->
                LyricsScreen(
                    song = song,
                    lyrics = lyrics,
                    progress = progress,
                    isSynced = lyrics?.contains("[") == true,
                    onNavigateBack = { navController.popBackStack() },
                    onToggleSynced = { }
                )
            } ?: LegalTextScreen(
                title = "Lyrics",
                body = "Start a song to view lyrics.",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Detail and utility screens
        composable(
            route = Screen.ArtistDetails.route,
            arguments = listOf(navArgument("artistId") { type = NavType.StringType })
        ) { backStackEntry ->
            ArtistDetailsScreen(
                artistId = backStackEntry.arguments?.getString("artistId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onAlbumClick = { navController.navigate(Screen.AlbumDetails.createRoute(it)) },
                onSongClick = { song, queue -> selectSong(song, queue) }
            )
        }

        composable(
            route = Screen.AlbumDetails.route,
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            AlbumDetailsScreen(
                albumId = backStackEntry.arguments?.getString("albumId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onSongClick = { song, queue -> selectSong(song, queue) },
                onAddToPlaylist = { song -> navController.navigate(Screen.AddToPlaylist.createRoute(song)) }
            )
        }

        composable(
            route = Screen.PlaylistDetails.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            PlaylistDetailsScreen(
                playlistId = backStackEntry.arguments?.getString("playlistId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onAddSongs = { navController.navigate(Screen.Search.route) },
                onSongClick = { song, queue -> selectSong(song, queue) }
            )
        }

        composable(
            route = Screen.GenreDetails.route,
            arguments = listOf(navArgument("genreId") { type = NavType.StringType })
        ) { backStackEntry ->
            val genreId = backStackEntry.arguments?.getString("genreId") ?: ""
            LegalTextScreen(
                title = "Genre",
                body = "Browse playlists, new releases, and top artists for genre $genreId. This screen is ready to connect to the online catalog and local cache.",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddToPlaylist.route,
            arguments = listOf(
                navArgument("songId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("artist") { type = NavType.StringType },
                navArgument("album") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            AddToPlaylistScreen(
                songId = backStackEntry.arguments?.getString("songId") ?: "",
                title = backStackEntry.arguments?.getString("title") ?: "Saved song",
                artist = backStackEntry.arguments?.getString("artist") ?: "Unknown Artist",
                album = backStackEntry.arguments?.getString("album") ?: "Unknown Album",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CastDevices.route) { CastDevicePickerScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.DownloadStorage.route) { DownloadStorageManagerScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.PrivacyPolicy.route) { LegalTextScreen("Privacy Policy", PrivacyPolicyBody, onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.Terms.route) { LegalTextScreen("Terms of Service", TermsBody, onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.NotificationPermission.route) {
            PermissionExplainerScreen(
                title = "Notifications",
                description = "Slow Music uses notifications for playback controls, download progress, and playback recovery.",
                permission = notificationPermissionName(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.LocalFilesPermission.route) {
            PermissionExplainerScreen(
                title = "Local music access",
                description = "Allow Slow Music to scan audio files stored on this device and add them to your local library database.",
                permission = Manifest.permission.READ_MEDIA_AUDIO,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
