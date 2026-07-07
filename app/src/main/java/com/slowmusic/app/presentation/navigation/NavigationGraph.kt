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
import com.slowmusic.app.presentation.screens.details.*

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    playbackState: PlaybackState,
    currentSong: Song?,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
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
                    // Navigate to player or play song
                    onPlayPause()
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
                onNavigateToSeeAll = { section ->
                    // Navigate to see all screen
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onSongClick = { song ->
                    onPlayPause()
                },
                onArtistClick = { artistId ->
                    navController.navigate(Screen.ArtistDetails.createRoute(artistId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.AlbumDetails.createRoute(albumId))
                },
                onGenreClick = { genreId ->
                    navController.navigate(Screen.GenreDetails.createRoute(genreId))
                }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onSongClick = { song ->
                    onPlayPause()
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
                }
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
                    onPlayPause()
                }
            )
        }

        composable(Screen.RecentPlays.route) {
            RecentPlaysScreen(
                onSongClick = { song ->
                    onPlayPause()
                }
            )
        }

        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onSongClick = { song ->
                    onPlayPause()
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
                onNavigateToCastDevices = { navController.navigate(Screen.CastDevices.route) }
            )
        }

        composable(Screen.Logs.route) {
            LogsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
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
                onSongClick = { onPlayPause() }
            )
        }

        composable(
            route = Screen.AlbumDetails.route,
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            AlbumDetailsScreen(
                albumId = backStackEntry.arguments?.getString("albumId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onSongClick = { onPlayPause() },
                onAddToPlaylist = { song -> navController.navigate(Screen.AddToPlaylist.createRoute(song.id)) }
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
            arguments = listOf(navArgument("songId") { type = NavType.StringType })
        ) { backStackEntry ->
            AddToPlaylistScreen(
                songId = backStackEntry.arguments?.getString("songId") ?: "",
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
