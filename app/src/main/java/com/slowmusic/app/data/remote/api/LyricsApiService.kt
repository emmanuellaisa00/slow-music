package com.slowmusic.app.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/** lyrics.ovh-compatible fallback API. */
interface LyricsApiService {
    @GET("v1/{artist}/{title}")
    suspend fun getLyrics(
        @retrofit2.http.Path("artist") artist: String,
        @retrofit2.http.Path("title") title: String
    ): LyricsResponse
}

data class LyricsResponse(
    @SerializedName("lyrics") val lyrics: String?
)

/** LRCLib API for plain or synced lyrics. */
interface LrcLibApiService {
    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrcLibSearchResult>

    @GET("api/get")
    suspend fun getLyrics(
        @Query("id") id: Int
    ): LrcLibLyrics
}

data class LrcLibSearchResult(
    @SerializedName("id") val id: Int,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("albumName") val albumName: String?
)

data class LrcLibLyrics(
    @SerializedName("id") val id: Int,
    @SerializedName("plainLyrics") val plainLyrics: String?,
    @SerializedName("syncedLyrics") val syncedLyrics: String?
)
