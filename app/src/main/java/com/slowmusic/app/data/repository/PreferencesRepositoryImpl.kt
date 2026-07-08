package com.slowmusic.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.Preferences.Key as PreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "slow_music_prefs")

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : PreferencesRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NAVIGATION_STYLE = stringPreferencesKey("navigation_style")
        val NETWORK_MODE = stringPreferencesKey("network_mode")
        val DOWNLOAD_ON_WIFI_ONLY = booleanPreferencesKey("download_on_wifi_only")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val CROSSFADE_ENABLED = booleanPreferencesKey("crossfade_enabled")
        val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val AUTO_PLAY_SIMILAR = booleanPreferencesKey("auto_play_similar")
        val UI_STYLE = stringPreferencesKey("ui_style")
        val RESOLVER_BACKEND_URL = stringPreferencesKey("resolver_backend_url")
        val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val EQUALIZER_PRESET = intPreferencesKey("equalizer_preset")
        val EQUALIZER_BANDS = stringPreferencesKey("equalizer_bands")
        val SEARCH_HISTORY = stringPreferencesKey("search_history")
        val FAVORITES = stringPreferencesKey("favorites")
        val RECENTLY_PLAYED = stringPreferencesKey("recently_played")
        val PLAY_COUNTS = stringPreferencesKey("play_counts")
        val PLAYLISTS = stringPreferencesKey("playlists")
        val DOWNLOADED_SONGS = stringPreferencesKey("downloaded_songs")
        val FOLLOWED_ARTISTS = stringPreferencesKey("followed_artists")
    }

    override fun getUserPreferences(): Flow<UserPreferences> {
        return context.dataStore.data.map { prefs ->
            UserPreferences(
                theme = ThemeMode.valueOf(prefs[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name),
                navigationStyle = NavigationStyle.valueOf(prefs[PreferencesKeys.NAVIGATION_STYLE] ?: NavigationStyle.TABS.name),
                downloadOnWifiOnly = prefs[PreferencesKeys.DOWNLOAD_ON_WIFI_ONLY] ?: true,
                audioQuality = AudioQuality.valueOf(prefs[PreferencesKeys.AUDIO_QUALITY] ?: AudioQuality.HIGH.name),
                crossfadeEnabled = prefs[PreferencesKeys.CROSSFADE_ENABLED] ?: false,
                crossfadeDuration = prefs[PreferencesKeys.CROSSFADE_DURATION] ?: 5,
                playbackSpeed = prefs[PreferencesKeys.PLAYBACK_SPEED] ?: 1f,
                autoPlaySimilar = prefs[PreferencesKeys.AUTO_PLAY_SIMILAR] ?: true,
                networkMode = NetworkMode.valueOf(prefs[PreferencesKeys.NETWORK_MODE] ?: NetworkMode.ONLINE_ONLY.name),
                resolverBackendUrl = prefs[PreferencesKeys.RESOLVER_BACKEND_URL] ?: "",
                uiStyle = UIStyle.valueOf(prefs[PreferencesKeys.UI_STYLE] ?: UIStyle.APPLE_MUSIC.name)
            )
        }
    }

    override suspend fun updateUserPreferences(preferences: UserPreferences) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = preferences.theme.name
            prefs[PreferencesKeys.NAVIGATION_STYLE] = preferences.navigationStyle.name
            prefs[PreferencesKeys.DOWNLOAD_ON_WIFI_ONLY] = preferences.downloadOnWifiOnly
            prefs[PreferencesKeys.AUDIO_QUALITY] = preferences.audioQuality.name
            prefs[PreferencesKeys.CROSSFADE_ENABLED] = preferences.crossfadeEnabled
            prefs[PreferencesKeys.CROSSFADE_DURATION] = preferences.crossfadeDuration
            prefs[PreferencesKeys.PLAYBACK_SPEED] = preferences.playbackSpeed
            prefs[PreferencesKeys.AUTO_PLAY_SIMILAR] = preferences.autoPlaySimilar
            prefs[PreferencesKeys.NETWORK_MODE] = preferences.networkMode.name
            prefs[PreferencesKeys.RESOLVER_BACKEND_URL] = preferences.resolverBackendUrl
            prefs[PreferencesKeys.UI_STYLE] = preferences.uiStyle.name
        }
    }

    override fun getNetworkMode(): Flow<NetworkMode> {
        return context.dataStore.data.map { prefs ->
            NetworkMode.valueOf(prefs[PreferencesKeys.NETWORK_MODE] ?: NetworkMode.ONLINE_ONLY.name)
        }
    }

    override suspend fun setNetworkMode(mode: NetworkMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.NETWORK_MODE] = mode.name
        }
    }

    override fun getEqualizerSettings(): Flow<EqualizerSettings> {
        return context.dataStore.data.map { prefs ->
            EqualizerSettings(
                enabled = prefs[PreferencesKeys.EQUALIZER_ENABLED] ?: false,
                preset = prefs[PreferencesKeys.EQUALIZER_PRESET] ?: 0,
                bandLevels = runCatching {
                    val type = object : TypeToken<Map<Int, Int>>() {}.type
                    gson.fromJson<Map<Int, Int>>(prefs[PreferencesKeys.EQUALIZER_BANDS] ?: "{}", type)
                }.getOrDefault(emptyMap())
            )
        }
    }

    override suspend fun updateEqualizerSettings(settings: EqualizerSettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.EQUALIZER_ENABLED] = settings.enabled
            prefs[PreferencesKeys.EQUALIZER_PRESET] = settings.preset
            prefs[PreferencesKeys.EQUALIZER_BANDS] = gson.toJson(settings.bandLevels)
        }
    }

    override fun getThemeMode(): Flow<ThemeMode> {
        return context.dataStore.data.map { prefs ->
            ThemeMode.valueOf(prefs[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name)
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    override fun getNavigationStyle(): Flow<NavigationStyle> {
        return context.dataStore.data.map { prefs ->
            NavigationStyle.valueOf(prefs[PreferencesKeys.NAVIGATION_STYLE] ?: NavigationStyle.TABS.name)
        }
    }

    override suspend fun setNavigationStyle(style: NavigationStyle) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.NAVIGATION_STYLE] = style.name
        }
    }

    override fun getSearchHistory(): Flow<List<String>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[PreferencesKeys.SEARCH_HISTORY] ?: "[]"
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    override suspend fun addToSearchHistory(query: String) {
        context.dataStore.edit { prefs ->
            val current = getSearchHistory().first().toMutableList()
            current.remove(query)
            current.add(0, query)
            val trimmed = current.take(20)
            prefs[PreferencesKeys.SEARCH_HISTORY] = gson.toJson(trimmed)
        }
    }

    override suspend fun clearSearchHistory() {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SEARCH_HISTORY] = "[]"
        }
    }
}

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : LibraryRepository {

    private object LibraryKeys {
        val FAVORITES = stringPreferencesKey("favorites")
        val RECENTLY_PLAYED = stringPreferencesKey("recently_played")
        val PLAY_COUNTS = stringPreferencesKey("play_counts")
        val PLAYLISTS = stringPreferencesKey("playlists")
        val DOWNLOADED_SONGS = stringPreferencesKey("downloaded_songs")
        val FOLLOWED_ARTISTS = stringPreferencesKey("followed_artists")
    }

    private fun <T> typeToken(): TypeToken<List<T>> = object : TypeToken<List<T>>() {}
    
    private suspend inline fun <reified T> getList(key: PreferencesKey<String>): List<T> {
        var result: List<T> = emptyList()
        context.dataStore.edit { prefs ->
            val json = prefs[key] ?: "[]"
            val type = object : TypeToken<List<T>>() {}.type
            result = gson.fromJson(json, type) ?: emptyList()
        }
        return result
    }

    private suspend inline fun <reified T> setList(key: PreferencesKey<String>, list: List<T>) {
        context.dataStore.edit { prefs ->
            prefs[key] = gson.toJson(list)
        }
    }

    // Favorites
    override fun getFavorites(): Flow<List<Song>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[LibraryKeys.FAVORITES] ?: "[]"
            val type = object : TypeToken<List<Song>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    override suspend fun addToFavorites(song: Song) {
        context.dataStore.edit { prefs ->
            val current = getFavorites().map { it }.first().toMutableList()
            if (current.none { it.id == song.id }) {
                current.add(0, song)
                prefs[LibraryKeys.FAVORITES] = gson.toJson(current)
            }
        }
    }

    override suspend fun removeFromFavorites(songId: String) {
        context.dataStore.edit { prefs ->
            val current = getFavorites().map { it }.first().toMutableList()
            current.removeAll { it.id == songId }
            prefs[LibraryKeys.FAVORITES] = gson.toJson(current)
        }
    }

    override suspend fun isFavorite(songId: String): Boolean {
        return getFavorites().first().any { it.id == songId }
    }

    // Recently Played
    override fun getRecentlyPlayed(): Flow<List<Song>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[LibraryKeys.RECENTLY_PLAYED] ?: "[]"
            val type = object : TypeToken<List<Song>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    override suspend fun addToRecentlyPlayed(song: Song) {
        context.dataStore.edit { prefs ->
            val current = getRecentlyPlayed().first().toMutableList()
            current.removeAll { it.id == song.id }
            current.add(0, song)
            val trimmed = current.take(50)
            prefs[LibraryKeys.RECENTLY_PLAYED] = gson.toJson(trimmed)
        }
    }

    override suspend fun clearRecentlyPlayed() {
        context.dataStore.edit { prefs ->
            prefs[LibraryKeys.RECENTLY_PLAYED] = "[]"
        }
    }

    // Play Counts
    override fun getMostPlayed(): Flow<List<Song>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[LibraryKeys.PLAY_COUNTS] ?: "{}"
            val type = object : TypeToken<Map<String, Int>>() {}.type
            val playCounts: Map<String, Int> = gson.fromJson(json, type) ?: emptyMap()
            
            val current = getRecentlyPlayed().first()
            current.sortedByDescending { playCounts[it.id] ?: 0 }
        }
    }

    override suspend fun incrementPlayCount(songId: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[LibraryKeys.PLAY_COUNTS] ?: "{}"
            val type = object : TypeToken<MutableMap<String, Int>>() {}.type
            val playCounts: MutableMap<String, Int> = gson.fromJson(json, type) ?: mutableMapOf()
            playCounts[songId] = (playCounts[songId] ?: 0) + 1
            prefs[LibraryKeys.PLAY_COUNTS] = gson.toJson(playCounts)
        }
    }

    // Playlists
    override fun getPlaylists(): Flow<List<Playlist>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[LibraryKeys.PLAYLISTS] ?: "[]"
            val type = object : TypeToken<List<Playlist>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    override suspend fun createPlaylist(name: String, description: String?): Playlist {
        val playlist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            artworkUrl = null,
            songIds = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        context.dataStore.edit { prefs ->
            val current = getPlaylists().first().toMutableList()
            current.add(playlist)
            prefs[LibraryKeys.PLAYLISTS] = gson.toJson(current)
        }
        return playlist
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        context.dataStore.edit { prefs ->
            val current = getPlaylists().first().toMutableList()
            val index = current.indexOfFirst { it.id == playlist.id }
            if (index >= 0) {
                current[index] = playlist.copy(updatedAt = System.currentTimeMillis())
                prefs[LibraryKeys.PLAYLISTS] = gson.toJson(current)
            }
        }
    }

    override suspend fun deletePlaylist(playlistId: String) {
        context.dataStore.edit { prefs ->
            val current = getPlaylists().first().toMutableList()
            current.removeAll { it.id == playlistId }
            prefs[LibraryKeys.PLAYLISTS] = gson.toJson(current)
        }
    }

    override suspend fun addSongToPlaylist(playlistId: String, song: Song) {
        context.dataStore.edit { prefs ->
            val current = getPlaylists().first().toMutableList()
            val index = current.indexOfFirst { it.id == playlistId }
            if (index >= 0) {
                val playlist = current[index]
                if (song.id !in playlist.songIds) {
                    current[index] = playlist.copy(
                        songIds = playlist.songIds + song.id,
                        updatedAt = System.currentTimeMillis()
                    )
                    prefs[LibraryKeys.PLAYLISTS] = gson.toJson(current)
                }
            }
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        context.dataStore.edit { prefs ->
            val current = getPlaylists().first().toMutableList()
            val index = current.indexOfFirst { it.id == playlistId }
            if (index >= 0) {
                val playlist = current[index]
                current[index] = playlist.copy(
                    songIds = playlist.songIds.filter { it != songId },
                    updatedAt = System.currentTimeMillis()
                )
                prefs[LibraryKeys.PLAYLISTS] = gson.toJson(current)
            }
        }
    }

    override suspend fun getPlaylistById(playlistId: String): Playlist? {
        return getPlaylists().first().find { it.id == playlistId }
    }

    // Downloads
    override fun getDownloadedSongs(): Flow<List<Song>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[LibraryKeys.DOWNLOADED_SONGS] ?: "[]"
            val type = object : TypeToken<List<Song>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    override suspend fun downloadSong(song: Song) {
        context.dataStore.edit { prefs ->
            val current = getDownloadedSongs().first().toMutableList()
            val index = current.indexOfFirst { it.id == song.id }
            val downloadedSong = song.copy(isDownloaded = true)
            if (index >= 0) {
                current[index] = downloadedSong
            } else {
                current.add(downloadedSong)
            }
            prefs[LibraryKeys.DOWNLOADED_SONGS] = gson.toJson(current)
        }
    }

    override suspend fun deleteDownload(songId: String) {
        context.dataStore.edit { prefs ->
            val current = getDownloadedSongs().first().toMutableList()
            current.removeAll { it.id == songId }
            prefs[LibraryKeys.DOWNLOADED_SONGS] = gson.toJson(current)
        }
    }

    override suspend fun isDownloaded(songId: String): Boolean {
        return getDownloadedSongs().first().any { it.id == songId }
    }

    // Followed Artists
    override fun getFollowedArtists(): Flow<List<Artist>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[LibraryKeys.FOLLOWED_ARTISTS] ?: "[]"
            val type = object : TypeToken<List<Artist>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    override suspend fun followArtist(artist: Artist) {
        context.dataStore.edit { prefs ->
            val current = getFollowedArtists().first().toMutableList()
            if (current.none { it.id == artist.id }) {
                current.add(artist.copy(isFollowed = true))
                prefs[LibraryKeys.FOLLOWED_ARTISTS] = gson.toJson(current)
            }
        }
    }

    override suspend fun unfollowArtist(artistId: String) {
        context.dataStore.edit { prefs ->
            val current = getFollowedArtists().first().toMutableList()
            current.removeAll { it.id == artistId }
            prefs[LibraryKeys.FOLLOWED_ARTISTS] = gson.toJson(current)
        }
    }

    override suspend fun isFollowing(artistId: String): Boolean {
        return getFollowedArtists().first().any { it.id == artistId }
    }
}
