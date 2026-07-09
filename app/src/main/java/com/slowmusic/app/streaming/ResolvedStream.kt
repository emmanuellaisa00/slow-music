package com.slowmusic.app.streaming

import com.slowmusic.app.domain.model.Album
import com.slowmusic.app.domain.model.Playlist
import com.slowmusic.app.domain.model.Song

data class ResolvedStream(
    val url: String,
    val userAgent: String,
    val headers: Map<String, String> = emptyMap(),
    val source: String = "fallback"
)

data class OnlineTrack(
    val videoId: String,
    val title: String,
    val artist: String,
    val album: String,
    val thumbnailUrl: String?,
    val durationMs: Long
) {
    fun toSong(): Song = Song(
        id = "yt_$videoId",
        title = title,
        artist = artist,
        album = album.ifBlank { artist },
        albumArtUrl = thumbnailUrl,
        previewUrl = null,
        streamUrl = null,
        duration = durationMs,
        genre = null,
        releaseDate = null
    )
}

data class OnlineAlbumResult(
    val browseId: String,
    val audioPlaylistId: String,
    val title: String,
    val artist: String,
    val year: String,
    val thumbnailUrl: String?
) {
    fun toAlbum(): Album = Album(
        id = "ytalbum_$audioPlaylistId",
        title = title,
        artist = artist,
        artistId = artist.hashCode().toString(),
        artworkUrl = thumbnailUrl,
        trackCount = 0,
        releaseDate = year.ifBlank { null },
        genre = null
    )
}

data class OnlinePlaylistResult(
    val playlistId: String,
    val title: String,
    val author: String,
    val trackCount: Int,
    val thumbnailUrl: String?
) {
    fun toPlaylist(): Playlist = Playlist(
        id = "ytpl_$playlistId",
        name = title,
        description = author.ifBlank { "Discovered playlist" },
        artworkUrl = thumbnailUrl,
        songIds = emptyList(),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        isUserCreated = false
    )
}
