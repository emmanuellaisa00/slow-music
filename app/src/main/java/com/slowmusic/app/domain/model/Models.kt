package com.slowmusic.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String?,
    val previewUrl: String?,
    val streamUrl: String?,
    val duration: Long,
    val genre: String?,
    val releaseDate: String?,
    val isLocal: Boolean = false,
    val localPath: String? = null,
    val isDownloaded: Boolean = false,
    val downloadProgress: Int = 0
) : Parcelable

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val genre: String?,
    val albumCount: Int = 0,
    val songCount: Int = 0,
    val isFollowed: Boolean = false
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String,
    val artworkUrl: String?,
    val trackCount: Int = 0,
    val releaseDate: String?,
    val genre: String?
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val artworkUrl: String?,
    val songIds: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
    val isUserCreated: Boolean = true
)

data class Genre(
    val id: String,
    val name: String,
    val imageUrl: String?
)

data class SearchResult(
    val songs: List<Song>,
    val artists: List<Artist>,
    val albums: List<Album>,
    val playlists: List<Playlist>
)

enum class PlaybackState {
    IDLE, PLAYING, PAUSED, BUFFERING, ERROR
}

enum class RepeatMode {
    OFF, ONE, ALL
}

data class QueueItem(
    val song: Song,
    val position: Int
)

data class Lyrics(
    val songId: String,
    val text: String,
    val source: String?
)

data class EqualizerSettings(
    val enabled: Boolean = false,
    val preset: Int = 0,
    val bandLevels: Map<Int, Int> = emptyMap()
)

data class SleepTimer(
    val minutes: Int,
    val startTime: Long,
    val remainingTime: Long
)

data class UserPreferences(
    val theme: ThemeMode = ThemeMode.DARK,
    val navigationStyle: NavigationStyle = NavigationStyle.TABS,
    val downloadOnWifiOnly: Boolean = true,
    val audioQuality: AudioQuality = AudioQuality.HIGH,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 5,
    val playbackSpeed: Float = 1f,
    val autoPlaySimilar: Boolean = true,
    val networkMode: NetworkMode = NetworkMode.ONLINE_ONLY,
    val resolverBackendUrl: String = "",
    // Apple Music Style Settings
    val uiStyle: UIStyle = UIStyle.DEFAULT,
    val enableGlassmorphism: Boolean = true,
    val enableLiquidGlass: Boolean = true,
    val enableDynamicColors: Boolean = true,
    val enableSmoothAnimations: Boolean = true,
    val cornerRadiusStyle: CornerRadiusStyle = CornerRadiusStyle.LARGE
)

enum class UIStyle {
    DEFAULT,
    APPLE_MUSIC,
    IOS_GLASS
}

enum class CornerRadiusStyle {
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class NavigationStyle {
    TABS, BOTTOM_NAV, DRAWER
}

enum class AudioQuality {
    LOW, MEDIUM, HIGH
}

enum class NetworkMode {
    ONLINE_ONLY, SMART_CACHING, DOWNLOAD_MODE
}

data class Subscription(
    val type: SubscriptionType,
    val isActive: Boolean,
    val expiresAt: Long?,
    val features: List<String>
)

enum class SubscriptionType {
    FREE, PREMIUM, FAMILY, STUDENT
}

data class Ad(
    val id: String,
    val type: AdType,
    val content: String?,
    val imageUrl: String?,
    val actionUrl: String?
)

enum class AdType {
    BANNER, INTERSTITIAL, REWARDED
}
