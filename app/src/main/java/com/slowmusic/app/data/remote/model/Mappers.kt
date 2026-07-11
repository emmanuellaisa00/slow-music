package com.slowmusic.app.data.remote.model

import com.slowmusic.app.domain.model.Album
import com.slowmusic.app.domain.model.Artist
import com.slowmusic.app.domain.model.Genre
import com.slowmusic.app.domain.model.Song

fun SongDto.toDomain(): Song {
    val baseArtwork = artworkUrl100 ?: artworkUrl60 ?: artworkUrl30
    
    return Song(
        id = trackId?.toString() ?: "",
        title = trackName ?: "Unknown Title",
        artist = artistName ?: "Unknown Artist",
        album = collectionName ?: "Unknown Album",
        albumArtUrl = baseArtwork?.replace("100x100", "600x600") ?: baseArtwork,
        previewUrl = previewUrl,
        streamUrl = previewUrl, // Preview URL metadata only; playback uses the app stream resolver
        duration = trackTimeMillis ?: 0L,
        genre = primaryGenreName,
        releaseDate = releaseDate,
        isLocal = false,
        isDownloaded = false
    )
}

fun ArtistDto.toDomain(): Artist {
    return Artist(
        id = artistId?.toString() ?: "",
        name = artistName ?: "Unknown Artist",
        imageUrl = null, // Artists don't have artwork in basic search
        genre = primaryGenreName,
        albumCount = 0,
        songCount = 0,
        isFollowed = false
    )
}

fun AlbumDto.toDomain(): Album {
    return Album(
        id = collectionId?.toString() ?: "",
        title = collectionName ?: "Unknown Album",
        artist = artistName ?: "Unknown Artist",
        artistId = artistId?.toString() ?: "",
        artworkUrl = artworkUrl100?.replace("100x100", "600x600") ?: artworkUrl100,
        trackCount = trackCount ?: 0,
        releaseDate = releaseDate,
        genre = primaryGenreName
    )
}

fun GenreDto.toDomain(): Genre {
    return Genre(
        id = id,
        name = name,
        imageUrl = imageUrl
    )
}

fun List<SongDto>.toSongDomainList(): List<Song> = map { it.toDomain() }

fun List<ArtistDto>.toArtistDomainList(): List<Artist> = map { it.toDomain() }

fun List<AlbumDto>.toAlbumDomainList(): List<Album> = map { it.toDomain() }

fun List<GenreDto>.toGenreDomainList(): List<Genre> = map { it.toDomain() }
