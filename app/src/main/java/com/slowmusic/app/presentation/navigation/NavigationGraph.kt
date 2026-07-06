package com.slowmusic.app.presentation.navigation

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
        
        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Logs.route) {
            LogsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Detail screens - placeholder implementations
        composable(
            route = Screen.ArtistDetails.route,
            arguments = listOf(navArgument("artistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
            // ArtistDetailsScreen(artistId = artistId)
        }
        
        composable(
            route = Screen.AlbumDetails.route,
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            // AlbumDetailsScreen(albumId = albumId)
        }
        
        composable(
            route = Screen.PlaylistDetails.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
            // PlaylistDetailsScreen(playlistId = playlistId)
        }
        
        composable(
            route = Screen.GenreDetails.route,
            arguments = listOf(navArgument("genreId") { type = NavType.StringType })
        ) { backStackEntry ->
            val genreId = backStackEntry.arguments?.getString("genreId") ?: ""
            // GenreDetailsScreen(genreId = genreId)
        }
    }
}
