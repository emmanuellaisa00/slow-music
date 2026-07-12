package com.slowmusic.app.presentation.navigation

import android.Manifest
import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.slowmusic.app.domain.model.PlaybackState
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.presentation.screens.home.HomeScreen
import com.slowmusic.app.presentation.screens.ios.*
import com.slowmusic.app.presentation.screens.library.*
import com.slowmusic.app.presentation.screens.profile.ProfileScreen
import com.slowmusic.app.presentation.screens.profile.AppleProfileScreen
import com.slowmusic.app.presentation.screens.search.SearchScreen
import com.slowmusic.app.presentation.screens.splash.SplashScreen
import com.slowmusic.app.presentation.screens.onboarding.OnboardingScreen
import com.slowmusic.app.presentation.screens.settings.LogsScreen
import com.slowmusic.app.presentation.screens.settings.SettingsScreen
import com.slowmusic.app.presentation.screens.settings.AppleMusicSettingsRoute
import com.slowmusic.app.presentation.screens.settings.EqualizerControlScreen
import com.slowmusic.app.presentation.screens.player.AppleMusicPlayerScreen
import com.slowmusic.app.presentation.screens.player.QueueScreen
import com.slowmusic.app.presentation.screens.player.LyricsScreen
import com.slowmusic.app.presentation.screens.details.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    playbackState: PlaybackState,
    currentSong: Song?,
    useAppleMusicUi: Boolean = false,
    useIosGlass: Boolean = false,
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
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onSaveQueueAsPlaylist: () -> Unit,
    onClearQueue: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val iosPushSpring = spring<androidx.compose.ui.unit.IntOffset>(
        dampingRatio = 0.86f,
        stiffness = Spring.StiffnessMediumLow
    )

    fun sameSong(a: Song?, b: Song): Boolean {
        if (a == null) return false
        if (a.id == b.id) return true
        val durationClose = a.duration <= 0 || b.duration <= 0 || kotlin.math.abs(a.duration - b.duration) <= 3000
        return a.title.equals(b.title, ignoreCase = true) && a.artist.equals(b.artist, ignoreCase = true) && durationClose
    }

    fun selectSong(song: Song, queue: List<Song>) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        if (sameSong(currentSong, song)) {
            navController.navigateModal(Screen.Player.route)
        } else {
            onPlaySong(song, queue.ifEmpty { listOf(song) })
        }
    }

    fun openRoot(route: String) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        navController.navigateRootTab(route)
    }
    fun openPush(route: String) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        navController.navigatePush(route)
    }
    fun openModal(route: String) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        navController.navigateModal(route)
    }

    fun smartSearchQueue(seed: Song, candidates: List<Song>): List<Song> {
        val seedArtist = seed.artist.lowercase().trim()
        val seedTitle = seed.title.lowercase().trim()
        val seedGenre = seed.genre?.lowercase()?.trim().orEmpty()
        val seedAlbum = seed.album.lowercase().trim()
        fun Song.identity() = title.lowercase().trim() to artist.lowercase().trim()
        fun Song.score(): Int {
            val artistNorm = artist.lowercase().trim()
            val titleNorm = title.lowercase().trim()
            val genreNorm = genre?.lowercase()?.trim().orEmpty()
            val albumNorm = album.lowercase().trim()
            var score = 0
            if (id == seed.id || identity() == seed.identity()) score += 10_000
            if (artistNorm == seedArtist) score += 1_200
            if (artistNorm.contains(seedArtist) || seedArtist.contains(artistNorm)) score += 700
            if (seedGenre.isNotBlank() && genreNorm == seedGenre) score += 550
            if (seedGenre.isNotBlank() && genreNorm.contains(seedGenre)) score += 380
            if (seedAlbum.isNotBlank() && albumNorm == seedAlbum) score += 260
            if (titleNorm.contains(seedArtist) || seedTitle.contains(artistNorm)) score += 240
            val seedTokens = seedTitle.split(' ', '-', '/', '&').filter { it.length > 3 }.toSet()
            score += titleNorm.split(' ', '-', '/', '&').count { it in seedTokens } * 50
            if (isDownloaded || isLocal) score += 70
            return score
        }
        val unique = (listOf(seed) + candidates).distinctBy { it.identity() }
        val related = unique
            .filterNot { it.id == seed.id || it.identity() == seed.identity() }
            .sortedWith(compareByDescending<Song> { it.score() }.thenBy { it.title })
            .take(24)
        return (listOf(seed) + related).distinctBy { it.identity() }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = {
            val target = targetState.destination.route
            val initial = initialState.destination.route
            when {
                target.isModalRoute() -> slideInVertically(animationSpec = iosPushSpring) { it } + fadeIn(tween(180))
                initial.isRootTabRoute() && target.isRootTabRoute() -> fadeIn(tween(180))
                else -> slideInHorizontally(animationSpec = iosPushSpring) { it / 3 } + fadeIn(tween(160))
            }
        },
        exitTransition = {
            val target = targetState.destination.route
            val initial = initialState.destination.route
            when {
                target.isModalRoute() -> fadeOut(tween(120))
                initial.isRootTabRoute() && target.isRootTabRoute() -> fadeOut(tween(120))
                else -> slideOutHorizontally(animationSpec = iosPushSpring) { -it / 5 } + fadeOut(tween(120))
            }
        },
        popEnterTransition = {
            val initial = initialState.destination.route
            when {
                initial.isModalRoute() -> fadeIn(tween(160))
                else -> slideInHorizontally(animationSpec = iosPushSpring) { -it / 3 } + fadeIn(tween(140))
            }
        },
        popExitTransition = {
            val initial = initialState.destination.route
            when {
                initial.isModalRoute() -> slideOutVertically(animationSpec = iosPushSpring) { it } + fadeOut(tween(140))
                else -> slideOutHorizontally(animationSpec = iosPushSpring) { it / 3 } + fadeOut(tween(120))
            }
        }
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
                        launchSingleTop = true
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
                        launchSingleTop = true
                    }
                }
            )
        }

        // Main Tabs
        composable(Screen.Home.route) {
            if (useIosGlass) {
                IosGlassHomeScreen(
                    onSongClick = { song, queue -> selectSong(song, queue) },
                    onNavigateToSearch = { openRoot(Screen.Search.route) },
                    onNavigateToSettings = { openModal(Screen.Settings.route) },
                    onMore = { song -> openModal(Screen.AddToPlaylist.createRoute(song)) }
                )
            } else {
                HomeScreen(
                    onSongClick = { song, queue -> selectSong(song, smartSearchQueue(song, queue)) },
                    onArtistClick = { artistId -> openPush(Screen.ArtistDetails.createRoute(artistId)) },
                    onAlbumClick = { albumId -> openPush(Screen.AlbumDetails.createRoute(albumId)) },
                    onGenreClick = { genreId -> openPush(Screen.GenreDetails.createRoute(genreId)) },
                    onNavigateToSearch = { openRoot(Screen.Search.route) },
                    onNavigateToSettings = { openModal(Screen.Settings.route) },
                    onNavigateToNotifications = { openModal(Screen.NotificationPermission.route) },
                    onNavigateToSeeAll = { openRoot(Screen.Search.route) },
                    onAddToPlaylist = { song -> openModal(Screen.AddToPlaylist.createRoute(song)) },
                    onPlayNext = { song -> onPlayNext(song) },
                    onAddToQueue = { song -> onAddToQueue(song) },
                    onDownload = { song -> onDownload(song) },
                    onShare = { }
                )
            }
        }

        composable(Screen.Search.route) {
            if (useIosGlass) {
                IosGlassSearchScreen(
                    onSongClick = { song, queue -> selectSong(song, smartSearchQueue(song, queue)) },
                    onArtistClick = { artistId -> openPush(Screen.ArtistDetails.createRoute(artistId)) },
                    onAlbumClick = { albumId -> openPush(Screen.AlbumDetails.createRoute(albumId)) },
                    onPlaylistClick = { playlistId -> openPush(Screen.PlaylistDetails.createRoute(playlistId)) }
                )
            } else {
            SearchScreen(
                onSongClick = { song, queue ->
                    selectSong(song, smartSearchQueue(song, queue))
                },
                onArtistClick = { artistId ->
                    openPush(Screen.ArtistDetails.createRoute(artistId))
                },
                onAlbumClick = { albumId ->
                    openPush(Screen.AlbumDetails.createRoute(albumId))
                },
                onPlaylistClick = { playlistId ->
                    openPush(Screen.PlaylistDetails.createRoute(playlistId))
                },
                onGenreClick = { genreId ->
                    openPush(Screen.GenreDetails.createRoute(genreId))
                },
                onAddToPlaylist = { song -> openModal(Screen.AddToPlaylist.createRoute(song)) },
                onPlayNext = { song -> onPlayNext(song) },
                onAddToQueue = { song -> onAddToQueue(song) },
                onDownload = { song -> onDownload(song) },
                onShare = { },
                onNotifications = { openModal(Screen.NotificationPermission.route) }
            )
            }
        }

        composable(Screen.Library.route) {
            if (useIosGlass) {
                IosGlassLibraryScreen(
                    onFavorites = { openPush(Screen.Favorites.route) },
                    onDownloads = { openPush(Screen.Downloads.route) },
                    onLocal = { openPush(Screen.LocalMusic.route) },
                    onPlaylists = { openPush(Screen.Playlists.route) },
                    onSettings = { openModal(Screen.Settings.route) }
                )
            } else {
            LibraryScreen(
                onSongClick = { song, queue ->
                    selectSong(song, queue)
                },
                onNavigateToFavorites = {
                    openPush(Screen.Favorites.route)
                },
                onNavigateToRecent = {
                    openPush(Screen.RecentPlays.route)
                },
                onNavigateToMostPlayed = {
                    openPush(Screen.MostPlayed.route)
                },
                onNavigateToDownloads = {
                    openPush(Screen.Downloads.route)
                },
                onNavigateToLocalMusic = {
                    openPush(Screen.LocalMusic.route)
                },
                onNavigateToPlaylists = {
                    openPush(Screen.Playlists.route)
                },
                onPlaylistClick = { playlistId ->
                    openPush(Screen.PlaylistDetails.createRoute(playlistId))
                },
                onNavigateToArtists = {
                    openPush(Screen.Artists.route)
                },
                onNavigateToAlbums = {
                    openPush(Screen.Albums.route)
                },
                onNavigateToSettings = {
                    openModal(Screen.Settings.route)
                },
                onAddToPlaylist = { song -> openModal(Screen.AddToPlaylist.createRoute(song)) },
                onPlayNext = { song -> onPlayNext(song) },
                onAddToQueue = { song -> onAddToQueue(song) },
                onDownload = { song -> onDownload(song) },
                onShare = { }
            )
            }
        }

        composable(Screen.Profile.route) {
            if (useIosGlass) {
                IosGlassProfileScreen(onSettings = { openModal(Screen.Settings.route) })
            } else if (useAppleMusicUi) {
                AppleProfileScreen(
                    subscription = com.slowmusic.app.domain.model.Subscription(
                        type = com.slowmusic.app.domain.model.SubscriptionType.FREE,
                        isActive = false,
                        expiresAt = null,
                        features = listOf("Local library", "Full-song streaming", "Cached discovery")
                    ),
                    onNavigateToSubscription = { openModal(Screen.Subscription.route) },
                    onNavigateToSettings = { openModal(Screen.Settings.route) }
                )
            } else {
                ProfileScreen(
                    onNavigateToSubscription = { openModal(Screen.Subscription.route) },
                    onNavigateToSettings = { openModal(Screen.Settings.route) }
                )
            }
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

        composable(Screen.Playlists.route) { PlaylistsScreen(onPlaylistClick = { openPush(Screen.PlaylistDetails.createRoute(it)) }) }

        composable(Screen.Artists.route) { FollowedArtistsScreen(onArtistClick = { openPush(Screen.ArtistDetails.createRoute(it)) }) }

        composable(Screen.Albums.route) { SavedAlbumsScreen(onAlbumClick = { openPush(Screen.AlbumDetails.createRoute(it)) }) }


        composable(Screen.Player.route) {
            val song = currentSong
            if (song == null) {
                LegalTextScreen(
                    title = "Now Playing",
                    body = "Choose a song from Home, Search, Library, Artist, or Album to start playback.",
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                key(song.id) {
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
                        onNavigateToLyrics = { openModal(Screen.Lyrics.route) },
                        onNavigateToQueue = { openModal(Screen.Queue.route) },
                        onNavigateToCast = { openModal(Screen.CastDevices.route) },
                        onMoreOptions = { openModal(Screen.AddToPlaylist.createRoute(song)) },
                        onShare = { }
                    )
                }
            }
        }

        // Settings
        composable(Screen.Settings.route) {
            if (useIosGlass) {
                IosGlassSettingsSkin {
                    SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogs = { openModal(Screen.Logs.route) },
                onNavigateToStorage = { openModal(Screen.DownloadStorage.route) },
                onNavigateToPrivacy = { openModal(Screen.PrivacyPolicy.route) },
                onNavigateToTerms = { openModal(Screen.Terms.route) },
                onNavigateToNotifications = { openModal(Screen.NotificationPermission.route) },
                onNavigateToLocalFilesPermission = { openModal(Screen.LocalFilesPermission.route) },
                onNavigateToCastDevices = { openModal(Screen.CastDevices.route) },
                onNavigateToEqualizer = { openModal(Screen.Equalizer.route) }
                    )
                }
            } else {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogs = { openModal(Screen.Logs.route) },
                    onNavigateToStorage = { openModal(Screen.DownloadStorage.route) },
                    onNavigateToPrivacy = { openModal(Screen.PrivacyPolicy.route) },
                    onNavigateToTerms = { openModal(Screen.Terms.route) },
                    onNavigateToNotifications = { openModal(Screen.NotificationPermission.route) },
                    onNavigateToLocalFilesPermission = { openModal(Screen.LocalFilesPermission.route) },
                    onNavigateToCastDevices = { openModal(Screen.CastDevices.route) },
                    onNavigateToEqualizer = { openModal(Screen.Equalizer.route) }
                )
            }
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
            if (useIosGlass) {
                IosGlassQueueScreen(
                    currentSong = currentSong,
                    queue = queue,
                    onSongClick = { song -> selectSong(song, queue) },
                    onRemoveFromQueue = onRemoveFromQueue,
                    onMoveQueueItem = onMoveQueueItem,
                    onClearQueue = onClearQueue,
                    onSaveAsPlaylist = onSaveQueueAsPlaylist,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
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
        }

        composable(Screen.Lyrics.route) {
            currentSong?.let { song ->
                if (useIosGlass) {
                    key(song.id) {
                        IosGlassLyricsScreen(
                            song = song,
                            lyrics = lyrics,
                            progress = progress,
                            onSeekToProgress = onSeek,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                } else {
                    key(song.id) {
                        LyricsScreen(
                            song = song,
                            lyrics = lyrics,
                            progress = progress,
                            isSynced = lyrics?.contains("[") == true,
                            onNavigateBack = { navController.popBackStack() },
                            onToggleSynced = { },
                            onSeekToProgress = onSeek
                        )
                    }
                }
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
            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
            if (useIosGlass) {
                IosGlassArtistDetailScreen(
                    artistId = artistId,
                    onNavigateBack = { navController.popBackStack() },
                    onAlbumClick = { openPush(Screen.AlbumDetails.createRoute(it)) },
                    onSongClick = { song, queue -> selectSong(song, queue) }
                )
            } else {
                ArtistDetailsScreen(
                    artistId = artistId,
                    onNavigateBack = { navController.popBackStack() },
                    onAlbumClick = { openPush(Screen.AlbumDetails.createRoute(it)) },
                    onSongClick = { song, queue -> selectSong(song, queue) }
                )
            }
        }

        composable(
            route = Screen.AlbumDetails.route,
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            if (useIosGlass) {
                IosGlassAlbumDetailScreen(
                    albumId = albumId,
                    onNavigateBack = { navController.popBackStack() },
                    onSongClick = { song, queue -> selectSong(song, queue) },
                    onAddToPlaylist = { song -> openModal(Screen.AddToPlaylist.createRoute(song)) }
                )
            } else {
                AlbumDetailsScreen(
                    albumId = albumId,
                    onNavigateBack = { navController.popBackStack() },
                    onSongClick = { song, queue -> selectSong(song, queue) },
                    onAddToPlaylist = { song -> openModal(Screen.AddToPlaylist.createRoute(song)) }
                )
            }
        }

        composable(
            route = Screen.PlaylistDetails.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
            if (useIosGlass) {
                IosGlassPlaylistDetailScreen(
                    playlistId = playlistId,
                    onNavigateBack = { navController.popBackStack() },
                    onAddSongs = { openRoot(Screen.Search.route) },
                    onSongClick = { song, queue -> selectSong(song, queue) }
                )
            } else {
                PlaylistDetailsScreen(
                    playlistId = playlistId,
                    onNavigateBack = { navController.popBackStack() },
                    onAddSongs = { openRoot(Screen.Search.route) },
                    onSongClick = { song, queue -> selectSong(song, queue) }
                )
            }
        }

        composable(
            route = Screen.GenreDetails.route,
            arguments = listOf(navArgument("genreId") { type = NavType.StringType })
        ) { backStackEntry ->
            val genreId = backStackEntry.arguments?.getString("genreId") ?: ""
            LegalTextScreen(
                title = "Genre",
                body = "Browse playlists, new releases, and top artists for genre $genreId. This screen is ready to use your saved cache and music discovery sources.",
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
