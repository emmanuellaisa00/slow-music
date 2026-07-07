package com.slowmusic.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.slowmusic.app.data.remote.api.ITunesApiService
import com.slowmusic.app.data.remote.model.GenreConstants
import com.slowmusic.app.data.remote.model.toAlbumDomainList
import com.slowmusic.app.data.remote.model.toArtistDomainList
import com.slowmusic.app.data.remote.model.toDomain
import com.slowmusic.app.data.remote.model.toGenreDomainList
import com.slowmusic.app.data.remote.model.toSongDomainList
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.LocalMusicRepository
import com.slowmusic.app.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val apiService: ITunesApiService
) : MusicRepository {

    override suspend fun search(query: String): SearchResult = withContext(Dispatchers.IO) {
        try {
            SearchResult(
                songs = searchSongs(query),
                artists = searchArtists(query),
                albums = searchAlbums(query),
                playlists = emptyList()
            )
        } catch (e: Exception) {
            SearchResult(emptyList(), emptyList(), emptyList(), emptyList())
        }
    }

    override suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.search(term = query)
            response.results
                .filter { it.wrapperType == "track" || it.kind == "song" }
                .toSongDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchArtists(query: String): List<Artist> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchArtist(query)
            response.results.toArtistDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchAlbums(query: String): List<Album> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchAlbum(query)
            response.results.toAlbumDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrendingSongs(): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.search(term = "trending music", limit = 50)
            response.results
                .filter { it.wrapperType == "track" || it.kind == "song" }
                .toSongDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTopSongs(): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.search(term = "top hits 2024", limit = 50)
            response.results
                .filter { it.wrapperType == "track" || it.kind == "song" }
                .toSongDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getNewReleases(): List<Album> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchAlbum(term = "new releases 2024", limit = 30)
            response.results.toAlbumDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getGenres(): List<Genre> {
        return GenreConstants.GENRES.toGenreDomainList()
    }

    override suspend fun getSongsByGenre(genreId: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val genreName = GenreConstants.GENRES.find { it.id == genreId }?.name ?: "pop"
            val response = apiService.search(term = "$genreName music", limit = 50)
            response.results
                .filter { it.wrapperType == "track" || it.kind == "song" }
                .toSongDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getRecommendations(): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.search(term = "recommended music", limit = 30)
            response.results
                .filter { it.wrapperType == "track" || it.kind == "song" }
                .toSongDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getSongById(id: String): Song? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSongById(id)
            response.results.firstOrNull()?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getSongsByArtist(artistId: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSongsByArtistId(artistId)
            response.results
                .filter { it.wrapperType == "track" || it.kind == "song" }
                .toSongDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getSongsByAlbum(albumId: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSongsByAlbumId(albumId)
            response.results
                .filter { it.wrapperType == "track" || it.kind == "song" }
                .toSongDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getArtistById(id: String): Artist? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getArtistById(id)
            response.results.firstOrNull()?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getTopArtists(): List<Artist> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchArtist(term = "popular artists", limit = 20)
            response.results.toArtistDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAlbumById(id: String): Album? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAlbumById(id)
            response.results.firstOrNull()?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getTopAlbums(): List<Album> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchAlbum(term = "top albums 2024", limit = 30)
            response.results.toAlbumDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@Singleton
class LocalMusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocalMusicRepository {

    override suspend fun getLocalSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val contentUri = ContentUris.withAppendedId(collection, id)
                
                val artworkUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                songs.add(
                    Song(
                        id = id.toString(),
                        title = cursor.getString(titleColumn) ?: "Unknown",
                        artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                        album = cursor.getString(albumColumn) ?: "Unknown Album",
                        albumArtUrl = artworkUri.toString(),
                        previewUrl = contentUri.toString(),
                        streamUrl = contentUri.toString(),
                        duration = cursor.getLong(durationColumn),
                        genre = null,
                        releaseDate = null,
                        isLocal = true,
                        localPath = cursor.getString(dataColumn),
                        isDownloaded = false
                    )
                )
            }
        }
        
        songs
    }

    override suspend fun getLocalArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val songs = getLocalSongs()
        songs.groupBy { it.artist }
            .map { (artistName, artistSongs) ->
                Artist(
                    id = artistName.hashCode().toString(),
                    name = artistName,
                    imageUrl = null,
                    genre = null,
                    albumCount = artistSongs.map { it.album }.distinct().size,
                    songCount = artistSongs.size,
                    isFollowed = false
                )
            }
    }

    override suspend fun getLocalAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val songs = getLocalSongs()
        songs.groupBy { it.album }
            .map { (albumName, albumSongs) ->
                val firstSong = albumSongs.first()
                Album(
                    id = albumName.hashCode().toString(),
                    title = albumName,
                    artist = firstSong.artist,
                    artistId = firstSong.artist.hashCode().toString(),
                    artworkUrl = firstSong.albumArtUrl,
                    trackCount = albumSongs.size,
                    releaseDate = null,
                    genre = null
                )
            }
    }

    override suspend fun getSongByPath(path: String): Song? = withContext(Dispatchers.IO) {
        val songs = getLocalSongs()
        songs.find { it.localPath == path }
    }
}
