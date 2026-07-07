package com.slowmusic.app.streaming

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PipedResolver @Inject constructor() {
    private val http = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val instances = listOf(
        "https://pipedapi.kavin.rocks",
        "https://pipedapi.adminforge.de",
        "https://pipedapi.syncpundit.io",
        "https://pipedapi-libre.kavin.rocks",
        "https://api-piped.mha.fi"
    )

    private val ua = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Mobile Safari/537.36"

    suspend fun resolve(videoId: String): ResolvedStream? = coroutineScope {
        instances.map { base -> async(Dispatchers.IO) { resolveOne(base, videoId) } }
            .awaitAll()
            .firstOrNull { it != null }
    }

    suspend fun resolveExhaustive(videoId: String): ResolvedStream? = withContext(Dispatchers.IO) {
        instances.asSequence().mapNotNull { resolveOne(it, videoId) }.firstOrNull()
    }


    suspend fun playlistTracks(playlistId: String, maxResults: Int = 100): List<OnlineTrack> = withContext(Dispatchers.IO) {
        for (base in instances) {
            val tracks = runCatching {
                val json = http.newCall(Request.Builder().url("$base/playlists/$playlistId").header("User-Agent", ua).build())
                    .execute().use { resp -> if (!resp.isSuccessful) return@runCatching emptyList<OnlineTrack>() else JSONObject(resp.body?.string().orEmpty()) }
                val arr = json.optJSONArray("relatedStreams") ?: return@runCatching emptyList<OnlineTrack>()
                (0 until arr.length()).mapNotNull { i ->
                    val o = arr.optJSONObject(i) ?: return@mapNotNull null
                    val url = o.optString("url")
                    val videoId = url.substringAfter("watch?v=", "").substringBefore('&').takeIf { it.length == 11 }
                        ?: o.optString("url").substringAfterLast('/').takeIf { it.length == 11 }
                        ?: return@mapNotNull null
                    OnlineTrack(
                        videoId = videoId,
                        title = o.optString("title").ifBlank { "Track $i" },
                        artist = o.optString("uploaderName").ifBlank { o.optString("uploader") }.ifBlank { "Unknown Artist" },
                        album = json.optString("name"),
                        thumbnailUrl = o.optString("thumbnail").ifBlank { null },
                        durationMs = o.optLong("duration", 0L) * 1000L
                    )
                }.take(maxResults)
            }.getOrDefault(emptyList())
            if (tracks.isNotEmpty()) return@withContext tracks
        }
        emptyList()
    }

    private fun resolveOne(base: String, videoId: String): ResolvedStream? = runCatching {
        val json = http.newCall(Request.Builder().url("$base/streams/$videoId").header("User-Agent", ua).build())
            .execute().use { resp -> if (!resp.isSuccessful) return@runCatching null else JSONObject(resp.body?.string().orEmpty()) }
        pickAudio(json.optJSONArray("audioStreams"))?.let { return@runCatching ResolvedStream(it, ua, mapOf("Referer" to "$base/"), "piped") }
        pickMuxed(json.optJSONArray("videoStreams"))?.let { return@runCatching ResolvedStream(it, ua, mapOf("Referer" to "$base/"), "piped-muxed") }
        json.optString("hls").takeIf { it.isNotBlank() }?.let { return@runCatching ResolvedStream(it, ua, mapOf("Referer" to "$base/"), "piped-hls") }
        null
    }.getOrNull()

    private fun pickAudio(arr: JSONArray?): String? {
        if (arr == null) return null
        var bestUrl: String? = null
        var bestBitrate = -1
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val url = o.optString("url").takeIf { it.isNotBlank() } ?: continue
            val br = o.optInt("bitrate", 0).takeIf { it > 0 } ?: o.optInt("abr", 0)
            if (br >= bestBitrate) { bestBitrate = br; bestUrl = url }
        }
        return bestUrl
    }

    private fun pickMuxed(arr: JSONArray?): String? {
        if (arr == null) return null
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            if (o.optBoolean("videoOnly", false)) continue
            val url = o.optString("url")
            if (url.isNotBlank()) return url
        }
        return null
    }
}
