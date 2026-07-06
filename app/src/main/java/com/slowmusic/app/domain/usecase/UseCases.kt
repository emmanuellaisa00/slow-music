package com.slowmusic.app.domain.usecase

import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Search Use Cases
class SearchAllUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(query: String): SearchResult {
        return musicRepository.search(query)
    }
}

class SearchSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(query: String): List<Song> {
        return musicRepository.searchSongs(query)
    }
}

// Browse Use Cases
class GetTrendingSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(): List<Song> {
        return musicRepository.getTrendingSongs()
    }
}

class GetRecommendationsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(): List<Song> {
        return musicRepository.getRecommendations()
    }
}

class GetGenresUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(): List<Genre> {
        return musicRepository.getGenres()
    }
}

class GetSongsByGenreUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(genreId: String): List<Song> {
        return musicRepository.getSongsByGenre(genreId)
    }
}

// Library Use Cases
class GetFavoritesUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return libraryRepository.getFavorites()
    }
}

class ToggleFavoriteUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(song: Song) {
        if (libraryRepository.isFavorite(song.id)) {
            libraryRepository.removeFromFavorites(song.id)
        } else {
            libraryRepository.addToFavorites(song)
        }
    }
}

class GetRecentlyPlayedUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return libraryRepository.getRecentlyPlayed()
    }
}

class AddToRecentlyPlayedUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(song: Song) {
        libraryRepository.addToRecentlyPlayed(song)
    }
}

class GetMostPlayedUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return libraryRepository.getMostPlayed()
    }
}

class IncrementPlayCountUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(songId: String) {
        libraryRepository.incrementPlayCount(songId)
    }
}

// Playlist Use Cases
class GetPlaylistsUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(): Flow<List<Playlist>> {
        return libraryRepository.getPlaylists()
    }
}

class CreatePlaylistUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(name: String, description: String? = null): Playlist {
        return libraryRepository.createPlaylist(name, description)
    }
}

class AddToPlaylistUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(playlistId: String, song: Song) {
        libraryRepository.addSongToPlaylist(playlistId, song)
    }
}

class RemoveFromPlaylistUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(playlistId: String, songId: String) {
        libraryRepository.removeSongFromPlaylist(playlistId, songId)
    }
}

// Download Use Cases
class GetDownloadedSongsUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return libraryRepository.getDownloadedSongs()
    }
}

class DownloadSongUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(song: Song) {
        libraryRepository.downloadSong(song)
    }
}

// Artist Use Cases
class GetFollowedArtistsUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(): Flow<List<Artist>> {
        return libraryRepository.getFollowedArtists()
    }
}

class ToggleFollowArtistUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(artist: Artist) {
        if (libraryRepository.isFollowing(artist.id)) {
            libraryRepository.unfollowArtist(artist.id)
        } else {
            libraryRepository.followArtist(artist)
        }
    }
}

// Search History Use Cases
class GetSearchHistoryUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    operator fun invoke(): Flow<List<String>> {
        return preferencesRepository.getSearchHistory()
    }
}

class AddToSearchHistoryUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(query: String) {
        preferencesRepository.addToSearchHistory(query)
    }
}

// Local Music Use Cases
class GetLocalMusicUseCase @Inject constructor(
    private val localMusicRepository: LocalMusicRepository
) {
    suspend operator fun invoke(): List<Song> {
        return localMusicRepository.getLocalSongs()
    }
}

// Preferences Use Cases
class GetUserPreferencesUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    operator fun invoke(): Flow<UserPreferences> {
        return preferencesRepository.getUserPreferences()
    }
}

class UpdatePreferencesUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(preferences: UserPreferences) {
        preferencesRepository.updateUserPreferences(preferences)
    }
}

// Lyrics Use Case
class GetLyricsUseCase @Inject constructor(
    private val lyricsRepository: LyricsRepository
) {
    suspend operator fun invoke(song: Song): Lyrics? {
        return lyricsRepository.getLyrics(song)
    }
}

// Subscription Use Cases
class GetSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(): Flow<Subscription> {
        return subscriptionRepository.getCurrentSubscription()
    }
}

class IsPremiumUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return subscriptionRepository.isPremium()
    }
}

// Song Details Use Case
class GetSongDetailsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(songId: String): Song? {
        return musicRepository.getSongById(songId)
    }
}

class GetArtistDetailsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(artistId: String): Artist? {
        return musicRepository.getArtistById(artistId)
    }
}

class GetAlbumDetailsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(albumId: String): Album? {
        return musicRepository.getAlbumById(albumId)
    }
}
