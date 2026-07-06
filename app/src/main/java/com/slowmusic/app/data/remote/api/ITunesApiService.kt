package com.slowmusic.app.data.remote.api

import com.slowmusic.app.data.remote.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApiService {
    
    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 50
    ): SearchResponse

    @GET("search")
    suspend fun searchArtist(
        @Query("term") term: String,
        @Query("entity") entity: String = "musicArtist",
        @Query("limit") limit: Int = 20
    ): ArtistSearchResponse

    @GET("search")
    suspend fun searchAlbum(
        @Query("term") term: String,
        @Query("entity") entity: String = "album",
        @Query("limit") limit: Int = 30
    ): AlbumSearchResponse

    @GET("search")
    suspend fun searchAll(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 25
    ): SearchResponse

    @GET("search")
    suspend fun getTopSongs(
        @Query("term") term: String = "top songs",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 50
    ): SearchResponse

    @GET("lookup")
    suspend fun getSongById(
        @Query("id") id: String,
        @Query("entity") entity: String = "song"
    ): SearchResponse

    @GET("lookup")
    suspend fun getArtistById(
        @Query("id") id: String,
        @Query("entity") entity: String = "musicArtist"
    ): ArtistSearchResponse

    @GET("lookup")
    suspend fun getAlbumById(
        @Query("id") id: String,
        @Query("entity") entity: String = "album"
    ): AlbumSearchResponse

    @GET("lookup")
    suspend fun getSongsByAlbumId(
        @Query("id") albumId: String,
        @Query("entity") entity: String = "song"
    ): SearchResponse

    @GET("lookup")
    suspend fun getSongsByArtistId(
        @Query("id") artistId: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 50
    ): SearchResponse
}
