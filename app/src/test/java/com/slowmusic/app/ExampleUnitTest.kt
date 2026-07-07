package com.slowmusic.app

import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.MusicRepository
import com.slowmusic.app.domain.repository.LibraryRepository
import com.slowmusic.app.domain.usecase.*
import com.slowmusic.app.data.repository.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

/**
 * Unit Tests for Domain Models
 */
class DomainModelTest {
    
    @Test
    fun `Song model creates correctly`() {
        val song = Song(
            id = "123",
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            albumArtUrl = "https://example.com/art.jpg",
            previewUrl = "https://example.com/preview.mp3",
            streamUrl = "https://example.com/stream.mp3",
            duration = 180000,
            genre = "Pop",
            releaseDate = "2024-01-01"
        )
        
        assertEquals("123", song.id)
        assertEquals("Test Song", song.title)
        assertEquals("Test Artist", song.artist)
        assertEquals("Test Album", song.album)
        assertEquals(180000, song.duration)
        assertFalse(song.isLocal)
        assertFalse(song.isDownloaded)
    }
    
    @Test
    fun `Playlist model creates correctly`() {
        val playlist = Playlist(
            id = "pl_123",
            name = "My Playlist",
            description = "Test description",
            artworkUrl = null,
            songIds = listOf("1", "2", "3"),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        assertEquals("pl_123", playlist.id)
        assertEquals("My Playlist", playlist.name)
        assertEquals(3, playlist.songIds.size)
        assertTrue(playlist.isUserCreated)
    }
    
    @Test
    fun `UserPreferences has correct defaults`() {
        val prefs = UserPreferences()
        
        assertEquals(ThemeMode.DARK, prefs.theme)
        assertEquals(NavigationStyle.TABS, prefs.navigationStyle)
        assertTrue(prefs.downloadOnWifiOnly)
        assertEquals(AudioQuality.HIGH, prefs.audioQuality)
        assertFalse(prefs.crossfadeEnabled)
        assertEquals(5, prefs.crossfadeDuration)
        assertTrue(prefs.autoPlaySimilar)
    }
    
    @Test
    fun `Subscription types are correct`() {
        assertEquals(4, SubscriptionType.values().size)
        assertNotNull(SubscriptionType.FREE)
        assertNotNull(SubscriptionType.PREMIUM)
        assertNotNull(SubscriptionType.FAMILY)
        assertNotNull(SubscriptionType.STUDENT)
    }
}

/**
 * Unit Tests for Use Cases
 */
class UseCaseTest {
    
    private val musicRepository = mock<MusicRepository>()
    private val libraryRepository = mock<LibraryRepository>()
    
    @Test
    fun `SearchAllUseCase returns SearchResult`() = runBlocking {
        // Given
        val expectedSongs = listOf(
            Song(
                id = "1",
                title = "Song 1",
                artist = "Artist 1",
                album = "Album 1",
                albumArtUrl = null,
                previewUrl = null,
                streamUrl = null,
                duration = 180000,
                genre = null,
                releaseDate = null
            )
        )
        `when`(musicRepository.search("test")).thenReturn(
            SearchResult(expectedSongs, emptyList(), emptyList(), emptyList())
        )
        
        val useCase = SearchAllUseCase(musicRepository)
        
        // When
        val result = useCase("test")
        
        // Then
        assertNotNull(result)
        assertEquals(1, result.songs.size)
        assertEquals("Song 1", result.songs.first().title)
    }
    
    @Test
    fun `GetFavoritesUseCase returns favorites flow`() = runBlocking {
        // Given
        val favorites = listOf(
            Song(
                id = "1",
                title = "Favorite Song",
                artist = "Artist",
                album = "Album",
                albumArtUrl = null,
                previewUrl = null,
                streamUrl = null,
                duration = 180000,
                genre = null,
                releaseDate = null
            )
        )
        `when`(libraryRepository.getFavorites()).thenReturn(flowOf(favorites))
        
        val useCase = GetFavoritesUseCase(libraryRepository)
        
        // When
        val result = useCase().first()
        
        // Then
        assertEquals(1, result.size)
        assertEquals("Favorite Song", result.first().title)
    }
    
    @Test
    fun `ToggleFavoriteUseCase adds to favorites`() = runBlocking {
        // Given
        val song = Song(
            id = "1",
            title = "Song",
            artist = "Artist",
            album = "Album",
            albumArtUrl = null,
            previewUrl = null,
            streamUrl = null,
            duration = 180000,
            genre = null,
            releaseDate = null
        )
        `when`(libraryRepository.isFavorite("1")).thenReturn(false)
        
        val useCase = ToggleFavoriteUseCase(libraryRepository)
        
        // When
        useCase(song)
        
        // Then
        verify(libraryRepository).addToFavorites(song)
    }
    
    @Test
    fun `CreatePlaylistUseCase creates playlist`() = runBlocking {
        // Given
        val useCase = CreatePlaylistUseCase(libraryRepository)
        
        // When
        val playlist = useCase("Test Playlist", "Description")
        
        // Then
        assertNotNull(playlist)
        assertEquals("Test Playlist", playlist.name)
        assertEquals("Description", playlist.description)
        verify(libraryRepository).createPlaylist("Test Playlist", "Description")
    }
}

/**
 * Unit Tests for Mappers
 */
class MapperTest {
    
    @Test
    fun `SongDto maps to Song correctly`() {
        val dto = com.slowmusic.app.data.remote.model.SongDto(
            trackId = 123L,
            trackName = "Test Song",
            artistName = "Test Artist",
            collectionName = "Test Album",
            artworkUrl30 = "https://example.com/30.jpg",
            artworkUrl60 = "https://example.com/60.jpg",
            artworkUrl100 = "https://example.com/100.jpg",
            previewUrl = "https://example.com/preview.mp3",
            trackTimeMillis = 180000L,
            primaryGenreName = "Pop",
            releaseDate = "2024-01-01T00:00:00Z",
            trackViewUrl = "https://example.com/track",
            wrapperType = "track",
            kind = "song"
        )
        
        val song = dto.toDomain()
        
        assertEquals("123", song.id)
        assertEquals("Test Song", song.title)
        assertEquals("Test Artist", song.artist)
        assertEquals("Test Album", song.album)
        assertEquals(180000L, song.duration)
        assertEquals("Pop", song.genre)
        assertNotNull(song.albumArtUrl)
        assertFalse(song.isLocal)
    }
    
    @Test
    fun `AlbumDto maps to Album correctly`() {
        val dto = com.slowmusic.app.data.remote.model.AlbumDto(
            collectionId = 456L,
            collectionName = "Test Album",
            artistName = "Test Artist",
            artistId = 789L,
            artworkUrl60 = "https://example.com/60.jpg",
            artworkUrl100 = "https://example.com/100.jpg",
            releaseDate = "2024-01-01T00:00:00Z",
            primaryGenreName = "Rock",
            trackCount = 10,
            collectionType = "Album",
            wrapperType = "collection"
        )
        
        val album = dto.toDomain()
        
        assertEquals("456", album.id)
        assertEquals("Test Album", album.title)
        assertEquals("Test Artist", album.artist)
        assertEquals("789", album.artistId)
        assertEquals(10, album.trackCount)
        assertEquals("Rock", album.genre)
    }
}

/**
 * Unit Tests for Utilities
 */
class UtilityTest {
    
    @Test
    fun `formatDuration formats correctly`() {
        assertEquals("3:00", com.slowmusic.app.util.TimeUtils.formatDuration(180000))
        assertEquals("0:00", com.slowmusic.app.util.TimeUtils.formatDuration(0))
        assertEquals("1:30", com.slowmusic.app.util.TimeUtils.formatDuration(90000))
        assertEquals("10:00", com.slowmusic.app.util.TimeUtils.formatDuration(600000))
    }
    
    @Test
    fun `formatDurationLong formats with hours`() {
        assertEquals("1:00:00", com.slowmusic.app.util.TimeUtils.formatDurationLong(3600000))
        assertEquals("1:30:00", com.slowmusic.app.util.TimeUtils.formatDurationLong(5400000))
        assertEquals("0:01:00", com.slowmusic.app.util.TimeUtils.formatDurationLong(60000))
    }
    
    @Test
    fun `truncate shortens long strings`() {
        assertEquals("Hello...", com.slowmusic.app.util.StringUtils.truncate("Hello World", 8))
        assertEquals("Hello", com.slowmusic.app.util.StringUtils.truncate("Hello", 10))
    }
    
    @Test
    fun `capitalize capitalizes words`() {
        assertEquals("Hello World", com.slowmusic.app.util.StringUtils.capitalize("hello world"))
        assertEquals("Test String", com.slowmusic.app.util.StringUtils.capitalize("TEST STRING"))
    }
}
