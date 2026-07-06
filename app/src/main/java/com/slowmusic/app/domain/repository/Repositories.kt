package com.slowmusic.app.domain.repository

import com.slowmusic.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    // Search
    suspend fun search(query: String): SearchResult
    suspend fun searchSongs(query: String): List<Song>
    suspend fun searchArtists(query: String): List<Artist>
    suspend fun searchAlbums(query: String): List<Album>
    
    // Browse
    suspend fun getTrendingSongs(): List<Song>
    suspend fun getTopSongs(): List<Song>
    suspend fun getNewReleases(): List<Album>
    suspend fun getGenres(): List<Genre>
    suspend fun getSongsByGenre(genreId: String): List<Song>
    suspend fun getRecommendations(): List<Song>
    
    // Songs
    suspend fun getSongById(id: String): Song?
    suspend fun getSongsByArtist(artistId: String): List<Song>
    suspend fun getSongsByAlbum(albumId: String): List<Song>
    
    // Artists
    suspend fun getArtistById(id: String): Artist?
    suspend fun getTopArtists(): List<Artist>
    
    // Albums
    suspend fun getAlbumById(id: String): Album?
    suspend fun getTopAlbums(): List<Album>
}

interface LocalMusicRepository {
    // Local files
    suspend fun getLocalSongs(): List<Song>
    suspend fun getLocalArtists(): List<Artist>
    suspend fun getLocalAlbums(): List<Album>
    suspend fun getSongByPath(path: String): Song?
}

interface LibraryRepository {
    // Favorites
    fun getFavorites(): Flow<List<Song>>
    suspend fun addToFavorites(song: Song)
    suspend fun removeFromFavorites(songId: String)
    suspend fun isFavorite(songId: String): Boolean
    
    // Recent plays
    fun getRecentlyPlayed(): Flow<List<Song>>
    suspend fun addToRecentlyPlayed(song: Song)
    suspend fun clearRecentlyPlayed()
    
    // Most played
    fun getMostPlayed(): Flow<List<Song>>
    suspend fun incrementPlayCount(songId: String)
    
    // Playlists
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String, description: String?): Playlist
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: String)
    suspend fun addSongToPlaylist(playlistId: String, song: Song)
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String)
    suspend fun getPlaylistById(playlistId: String): Playlist?
    
    // Downloads
    fun getDownloadedSongs(): Flow<List<Song>>
    suspend fun downloadSong(song: Song)
    suspend fun deleteDownload(songId: String)
    suspend fun isDownloaded(songId: String): Boolean
    
    // Followed artists
    fun getFollowedArtists(): Flow<List<Artist>>
    suspend fun followArtist(artist: Artist)
    suspend fun unfollowArtist(artistId: String)
    suspend fun isFollowing(artistId: String): Boolean
}

interface PreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateUserPreferences(preferences: UserPreferences)
    
    fun getNetworkMode(): Flow<NetworkMode>
    suspend fun setNetworkMode(mode: NetworkMode)
    
    fun getEqualizerSettings(): Flow<EqualizerSettings>
    suspend fun updateEqualizerSettings(settings: EqualizerSettings)
    
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    
    fun getNavigationStyle(): Flow<NavigationStyle>
    suspend fun setNavigationStyle(style: NavigationStyle)
    
    fun getSearchHistory(): Flow<List<String>>
    suspend fun addToSearchHistory(query: String)
    suspend fun clearSearchHistory()
}

interface SubscriptionRepository {
    fun getCurrentSubscription(): Flow<Subscription>
    suspend fun purchaseSubscription(type: SubscriptionType)
    suspend fun cancelSubscription()
    fun isPremium(): Flow<Boolean>
}

interface LyricsRepository {
    suspend fun getLyrics(song: Song): Lyrics?
}

interface AdRepository {
    fun getAvailableAds(): Flow<List<Ad>>
    suspend fun recordAdImpression(adId: String)
    suspend fun recordAdClick(adId: String)
}
