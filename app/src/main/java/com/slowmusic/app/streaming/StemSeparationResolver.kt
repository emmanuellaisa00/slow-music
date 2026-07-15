package com.slowmusic.app.streaming

import com.google.gson.Gson
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StemSeparationResolver @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    @Volatile
    private var backendUrl: String? = null

    private val cache = ConcurrentHashMap<String, StemResolution>()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun setBackendUrl(url: String) {
        backendUrl = url.trim().trimEnd('/').ifBlank { null }
    }

    suspend fun resolveStems(song: Song): StemResolution? = withContext(Dispatchers.IO) {
        val existing = cache[song.stemCacheKey()]
        if (existing != null && (existing.vocalsUrl != null || existing.instrumentalUrl != null)) return@withContext existing

        val baseUrl = backendUrl ?: return@withContext null
        val sourceUrl = song.streamUrl?.takeIf { it.isNotBlank() }
            ?: song.previewUrl?.takeIf { it.isNotBlank() }
            ?: return@withContext null

        val requestPayload = StemRequest(
            songId = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            artworkUrl = song.albumArtUrl,
            sourceUrl = sourceUrl,
            durationMs = song.duration
        )

        runCatching {
            val body = gson.toJson(requestPayload).toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$baseUrl/stems/resolve")
                .post(body)
                .header("Accept", "application/json")
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Logger.w("StemSeparation", "Stem backend failed ${response.code} for ${song.title}")
                    return@withContext null
                }
                val text = response.body?.string().orEmpty()
                val parsed = gson.fromJson(text, StemResponse::class.java)
                val resolution = StemResolution(
                    vocalsUrl = parsed.vocalsUrl?.takeIf { it.isNotBlank() },
                    instrumentalUrl = parsed.instrumentalUrl?.takeIf { it.isNotBlank() }
                )
                cache[song.stemCacheKey()] = resolution
                resolution
            }
        }.onFailure {
            Logger.w("StemSeparation", "Stem backend exception for ${song.title}: ${it.message}")
        }.getOrNull()
    }

    data class StemResolution(
        val vocalsUrl: String?,
        val instrumentalUrl: String?
    )

    private data class StemRequest(
        val songId: String,
        val title: String,
        val artist: String,
        val album: String,
        val artworkUrl: String?,
        val sourceUrl: String,
        val durationMs: Long
    )

    private data class StemResponse(
        val vocalsUrl: String? = null,
        val instrumentalUrl: String? = null,
        val status: String? = null,
        val message: String? = null
    )

    private fun Song.stemCacheKey(): String = listOf(id, title.lowercase(), artist.lowercase()).joinToString("|")
}
