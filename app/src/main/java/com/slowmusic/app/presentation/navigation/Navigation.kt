package com.slowmusic.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import android.net.Uri
import com.slowmusic.app.domain.model.Song

sealed class Screen(val route: String) {
    // Main tabs
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Profile : Screen("profile")
    
    // Detail screens
    object SongDetails : Screen("song/{songId}") {
        fun createRoute(songId: String) = "song/$songId"
    }
    object ArtistDetails : Screen("artist/{artistId}") {
        fun createRoute(artistId: String) = "artist/$artistId"
    }
    object AlbumDetails : Screen("album/{albumId}") {
        fun createRoute(albumId: String) = "album/$albumId"
    }
    object PlaylistDetails : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: String) = "playlist/$playlistId"
    }
    object GenreDetails : Screen("genre/{genreId}") {
        fun createRoute(genreId: String) = "genre/$genreId"
    }
    
    // Library sub-screens
    object Favorites : Screen("library/favorites")
    object RecentPlays : Screen("library/recent")
    object MostPlayed : Screen("library/most_played")
    object Downloads : Screen("library/downloads")
    object LocalMusic : Screen("library/local")
    object Playlists : Screen("library/playlists")
    object Artists : Screen("library/artists")
    object Albums : Screen("library/albums")
    
    // Player screens
    object Player : Screen("player")
    object Queue : Screen("queue")
    object Lyrics : Screen("lyrics")
    
    // Settings
    object Settings : Screen("settings")
    object Appearance : Screen("settings/appearance")
    object Playback : Screen("settings/playback")
    object DownloadsSettings : Screen("settings/downloads")
    object Equalizer : Screen("settings/equalizer")
    object Network : Screen("settings/network")
    object Subscription : Screen("settings/subscription")
    object About : Screen("settings/about")
    object Logs : Screen("settings/logs")
    object DownloadStorage : Screen("settings/download_storage")
    object PrivacyPolicy : Screen("legal/privacy")
    object Terms : Screen("legal/terms")
    object NotificationPermission : Screen("permissions/notifications")
    object LocalFilesPermission : Screen("permissions/local_files")
    object CastDevices : Screen("cast/devices")
    
    // Search
    object VoiceSearch : Screen("search/voice")
    object SearchResults : Screen("search/results/{query}") {
        fun createRoute(query: String) = "search/results/$query"
    }
    object AddToPlaylist : Screen("playlist/add/{songId}/{title}/{artist}/{album}") {
        fun createRoute(song: Song): String = "playlist/add/${Uri.encode(song.id)}/${Uri.encode(song.title)}/${Uri.encode(song.artist)}/${Uri.encode(song.album)}"
        fun createRoute(songId: String): String = "playlist/add/${Uri.encode(songId)}/${Uri.encode("Saved song")}/${Uri.encode("Unknown Artist")}/${Uri.encode("Unknown Album")}"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Search,
        title = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    BottomNavItem(
        screen = Screen.Library,
        title = "Library",
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic
    ),
    BottomNavItem(
        screen = Screen.Profile,
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

object LibraryItems {
    val items = listOf(
        LibraryItem(Screen.Favorites, "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        LibraryItem(Screen.RecentPlays, "Recently Played", Icons.Filled.History, Icons.Outlined.History),
        LibraryItem(Screen.MostPlayed, "Most Played", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp),
        LibraryItem(Screen.Downloads, "Downloads", Icons.Filled.Download, Icons.Outlined.Download),
        LibraryItem(Screen.LocalMusic, "Local Music", Icons.Filled.Smartphone, Icons.Outlined.Smartphone),
        LibraryItem(Screen.Playlists, "Playlists", Icons.Filled.QueueMusic, Icons.Outlined.QueueMusic),
        LibraryItem(Screen.Artists, "Artists", Icons.Filled.Artists, Icons.Outlined.Artists),
        LibraryItem(Screen.Albums, "Albums", Icons.Filled.Album, Icons.Outlined.Album)
    )
}

data class LibraryItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

object SettingsItems {
    val items = listOf(
        SettingsItem(Screen.Appearance, "Appearance", Icons.Filled.Palette, Icons.Outlined.Palette),
        SettingsItem(Screen.Playback, "Playback", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle),
        SettingsItem(Screen.DownloadsSettings, "Downloads", Icons.Filled.Download, Icons.Outlined.Download),
        SettingsItem(Screen.Equalizer, "Equalizer", Icons.Filled.Equalizer, Icons.Outlined.Equalizer),
        SettingsItem(Screen.Network, "Network", Icons.Filled.Wifi, Icons.Outlined.Wifi),
        SettingsItem(Screen.Subscription, "Subscription", Icons.Filled.CardMembership, Icons.Outlined.CardMembership),
        SettingsItem(Screen.Logs, "Logs", Icons.Filled.BugReport, Icons.Outlined.BugReport),
        SettingsItem(Screen.About, "About", Icons.Filled.Info, Icons.Outlined.Info)
    )
}

data class SettingsItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
