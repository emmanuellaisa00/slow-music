package com.slowmusic.app.presentation.navigation

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.slowmusic.app.presentation.screens.settings.LogsScreen
import com.slowmusic.app.presentation.screens.settings.SettingsScreen
import com.slowmusic.app.presentation.screens.player.AppleMusicPlayerScreen
import com.slowmusic.app.presentation.screens.details.*

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    playbackState: PlaybackState,
    currentSong: Song?,
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
    onDownload: (Song) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Main Tabs
        composable(Screen.Home.route) {
            HomeScreen(
                onSongClick = { song ->
                    onPlaySong(song, listOf(song))
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
                onAddToQueue = { song -> onPlaySong(song, listOfNotNull(currentSong, song).distinctBy { it.id }) },
                onDownload = { song -> onDownload(song) },
                onShare = { }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onSongClick = { song ->
                    onPlaySong(song, listOf(song))
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
                onAddToPlaylist = { song -> navController.navigate(Screen.AddToPlaylist.createRoute(song)) },
                onAddToQueue = { song -> onPlaySong(song, listOfNotNull(currentSong, song).distinctBy { it.id }) },
                onDownload = { song -> onDownload(song) },
                onShare = { }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onSongClick = { song ->
                    onPlaySong(song, listOf(song))
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
                onAddToQueue = { song -> onPlaySong(song, listOfNotNull(currentSong, song).distinctBy { it.id }) },
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
                onSongClick = { song ->
                    onPlaySong(song, listOf(song))
                }
            )
        }

        composable(Screen.RecentPlays.route) {
            RecentPlaysScreen(
                onSongClick = { song ->
                    onPlaySong(song, listOf(song))
                }
            )
        }

        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onSongClick = { song ->
                    onPlaySong(song, listOf(song))
                }
            )
        }


        composable(Screen.MostPlayed.route) {
            LegalTextScreen(
                title = "Most Played",
                body = "Your most played songs are calculated from the local play-count database. Play more music to build this chart.",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LocalMusic.route) {
            LegalTextScreen(
                title = "Local Music",
                body = "Local files are scanned from device audio storage after permission is granted. This screen is ready for the local media database and import flow.",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Playlists.route) {
            LegalTextScreen(
                title = "Playlists",
                body = "Create, edit, and open playlists from your local database. Open an individual playlist from Library to edit its name and songs.",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Artists.route) {
            LegalTextScreen(
                title = "Followed Artists",
                body = "Artists you follow are stored locally for now and can be synced to an online account later.",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Albums.route) {
            LegalTextScreen(
                title = "Saved Albums",
                body = "Saved albums and their cached metadata will appear here. Album detail screens are now implemented for online catalog results.",
                onNavigateBack = { navController.popBackStack() }
            )
        }


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
            LegalTextScreen(
                title = "Equalizer",
                body = "The audio effect engine is attached to the playback audio session. Presets and band sliders can now be wired to EqualizerManager.apply(settings) for device-specific tuning.",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Logs.route) {
            LogsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }


        composable(Screen.Queue.route) {
            LegalTextScreen(
                title = "Queue",
                body = "The active queue is managed by the player. Queue reorder/remove UI is ready to be connected to the Media3 queue controller.",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Lyrics.route) {
            LegalTextScreen(
                title = "Lyrics",
                body = "Lyrics are fetched from LRCLib with lyrics.ovh fallback. Select a song, then this screen can render synced lyric lines for the active track.",
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
                onSongClick = { song -> onPlaySong(song, listOf(song)) }
            )
        }

        composable(
            route = Screen.AlbumDetails.route,
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            AlbumDetailsScreen(
                albumId = backStackEntry.arguments?.getString("albumId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onSongClick = { song -> onPlaySong(song, listOf(song)) },
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
                onAddSongs = { navController.navigate(Screen.Search.route) }
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
