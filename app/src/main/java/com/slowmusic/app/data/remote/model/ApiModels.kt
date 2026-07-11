package com.slowmusic.app.data.remote.model

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<SongDto>
)

data class ArtistSearchResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<ArtistDto>
)

data class AlbumSearchResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<AlbumDto>
)

data class SongDto(
    @SerializedName("trackId") val trackId: Long?,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("collectionName") val collectionName: String?,
    @SerializedName("artworkUrl30") val artworkUrl30: String?,
    @SerializedName("artworkUrl60") val artworkUrl60: String?,
    @SerializedName("artworkUrl100") val artworkUrl100: String?,
    @SerializedName("previewUrl") val previewUrl: String?,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long?,
    @SerializedName("primaryGenreName") val primaryGenreName: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("trackViewUrl") val trackViewUrl: String?,
    @SerializedName("wrapperType") val wrapperType: String?,
    @SerializedName("kind") val kind: String?
)

data class ArtistDto(
    @SerializedName("artistId") val artistId: Long?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("artistLinkUrl") val artistLinkUrl: String?,
    @SerializedName("artistType") val artistType: String?,
    @SerializedName("primaryGenreName") val primaryGenreName: String?,
    @SerializedName("primaryGenreId") val primaryGenreId: Long?,
    @SerializedName("wrapperType") val wrapperType: String?
)

data class AlbumDto(
    @SerializedName("collectionId") val collectionId: Long?,
    @SerializedName("collectionName") val collectionName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("artistId") val artistId: Long?,
    @SerializedName("artworkUrl60") val artworkUrl60: String?,
    @SerializedName("artworkUrl100") val artworkUrl100: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("primaryGenreName") val primaryGenreName: String?,
    @SerializedName("trackCount") val trackCount: Int?,
    @SerializedName("collectionType") val collectionType: String?,
    @SerializedName("wrapperType") val wrapperType: String?
)

// Genre constants
object GenreConstants {
    val GENRES = listOf(
        GenreDto("20", "Action", null),
        GenreDto("2", "Afrikaans", null),
        GenreDto("3", "Alternative", null),
        GenreDto("4", "Ambient", null),
        GenreDto("5", "Blues", null),
        GenreDto("6", "Brazilian", null),
        GenreDto("7", "Children's Music", null),
        GenreDto("8", "Chinese", null),
        GenreDto("9", "Classical", null),
        GenreDto("10", "Country", null),
        GenreDto("11", "Dance", null),
        GenreDto("12", "Easy Listening", null),
        GenreDto("13", "Electronic", null),
        GenreDto("14", "Holiday", null),
        GenreDto("15", "Folk", null),
        GenreDto("16", "Hip Hop/Rap", null),
        GenreDto("17", "Holiday", null),
        GenreDto("18", "Jazz", null),
        GenreDto("19", "Latin", null),
        GenreDto("21", "New Age", null),
        GenreDto("22", "Opera", null),
        GenreDto("23", "Pop", null),
        GenreDto("24", "R&B/Soul", null),
        GenreDto("25", "Reggae", null),
        GenreDto("26", "Rock", null),
        GenreDto("27", "Singer/Songwriter", null),
        GenreDto("28", "Soundtrack", null),
        GenreDto("29", "Spoken Word", null),
        GenreDto("30", "World", null)
    )
}

data class GenreDto(
    val id: String,
    val name: String,
    val imageUrl: String?
)
