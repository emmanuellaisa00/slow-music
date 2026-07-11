package com.slowmusic.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.slowmusic.app.data.remote.model.GenreConstants
import com.slowmusic.app.data.remote.model.toGenreDomainList
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
    private val streamingFallbackResolver: com.slowmusic.app.streaming.StreamingFallbackResolver
) : MusicRepository {

    override suspend fun search(query: String): SearchResult = withContext(Dispatchers.IO) {
        SearchResult(
            songs = searchSongs(query),
            artists = searchArtists(query),
            albums = searchAlbums(query),
            playlists = streamingFallbackResolver.searchPlaylists(query)
        )
    }

    override suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        streamingFallbackResolver.searchSongs(query, 50)
    }

    override suspend fun searchArtists(query: String): List<Artist> = withContext(Dispatchers.IO) {
        val songs = streamingFallbackResolver.searchSongs(query, 40)
        songs.groupBy { it.artist.ifBlank { "Unknown Artist" } }
            .map { (name, artistSongs) ->
                Artist(
                    id = name.replace("/", "_").replace(" ", "_"),
                    name = name,
                    imageUrl = artistSongs.firstOrNull { !it.albumArtUrl.isNullOrBlank() }?.albumArtUrl,
                    genre = artistSongs.firstOrNull()?.genre,
                    albumCount = artistSongs.map { it.album }.filter { it.isNotBlank() }.distinct().size,
                    songCount = artistSongs.size
                )
            }
    }

    override suspend fun searchAlbums(query: String): List<Album> = withContext(Dispatchers.IO) {
        streamingFallbackResolver.searchAlbums(query, 30)
    }

    override suspend fun getTrendingSongs(): List<Song> = withContext(Dispatchers.IO) {
        streamingFallbackResolver.searchSongs("trending music global", 50)
    }

    override suspend fun getTopSongs(): List<Song> = withContext(Dispatchers.IO) {
        streamingFallbackResolver.searchSongs("top hits today", 50)
    }

    override suspend fun getNewReleases(): List<Album> = withContext(Dispatchers.IO) {
        streamingFallbackResolver.searchAlbums("new music releases", 30)
    }

    override suspend fun getGenres(): List<Genre> {
        return GenreConstants.GENRES.toGenreDomainList()
    }

    override suspend fun getSongsByGenre(genreId: String): List<Song> = withContext(Dispatchers.IO) {
        val genreName = GenreConstants.GENRES.find { it.id == genreId }?.name ?: genreId
        streamingFallbackResolver.searchSongs("$genreName music", 50)
    }

    override suspend fun getRecommendations(): List<Song> = withContext(Dispatchers.IO) {
        streamingFallbackResolver.searchSongs("recommended music mix", 40)
    }

    override suspend fun getSongById(id: String): Song? = withContext(Dispatchers.IO) {
        if (id.startsWith("yt_")) {
            streamingFallbackResolver.searchSongs(id.removePrefix("yt_"), 1).firstOrNull()?.copy(id = id)
        } else {
            streamingFallbackResolver.searchSongs(id, 1).firstOrNull()
        }
    }

    override suspend fun getSongsByArtist(artistId: String): List<Song> = withContext(Dispatchers.IO) {
        val artistName = artistId.replace("_", " ")
        streamingFallbackResolver.searchSongs("$artistName songs", 50)
    }

    override suspend fun getSongsByAlbum(albumId: String): List<Song> = withContext(Dispatchers.IO) {
        if (albumId.startsWith("ytalbum_") || albumId.startsWith("ytpl_")) {
            streamingFallbackResolver.playlistSongs(albumId)
        } else {
            streamingFallbackResolver.searchSongs(albumId, 40)
        }
    }

    override suspend fun getArtistById(id: String): Artist? = withContext(Dispatchers.IO) {
        val artistName = id.replace("_", " ")
        val songs = streamingFallbackResolver.searchSongs("$artistName songs", 30)
        Artist(
            id = id,
            name = songs.firstOrNull()?.artist?.takeIf { it.isNotBlank() } ?: artistName,
            imageUrl = songs.firstOrNull { !it.albumArtUrl.isNullOrBlank() }?.albumArtUrl,
            genre = songs.firstOrNull()?.genre,
            albumCount = songs.map { it.album }.filter { it.isNotBlank() }.distinct().size,
            songCount = songs.size
        )
    }

    override suspend fun getTopArtists(): List<Artist> = withContext(Dispatchers.IO) {
        searchArtists("popular music artists")
    }

    override suspend fun getAlbumById(id: String): Album? = withContext(Dispatchers.IO) {
        if (id.startsWith("ytalbum_") || id.startsWith("ytpl_")) {
            val songs = streamingFallbackResolver.playlistSongs(id)
            val first = songs.firstOrNull()
            Album(
                id = id,
                title = first?.album ?: "Album",
                artist = first?.artist ?: "Music",
                artistId = (first?.artist ?: id).hashCode().toString(),
                artworkUrl = first?.albumArtUrl,
                trackCount = songs.size,
                releaseDate = null,
                genre = first?.genre
            )
        } else {
            streamingFallbackResolver.searchAlbums(id, 1).firstOrNull()
        }
    }

    override suspend fun getTopAlbums(): List<Album> = withContext(Dispatchers.IO) {
        streamingFallbackResolver.searchAlbums("top albums", 30)
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

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 30000"
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
