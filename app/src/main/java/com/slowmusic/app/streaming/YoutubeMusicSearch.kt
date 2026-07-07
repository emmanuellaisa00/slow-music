package com.slowmusic.app.streaming

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeMusicSearch @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val endpoint = "https://music.youtube.com/youtubei/v1/search?prettyPrint=false"
    private val songsParams = "EgWKAQIIAWoQEAMQBBAJEAoQERAQEBUQHg=="
    private val albumsParams = "EgWKAQIYAWoQEAMQBBAJEAoQERAQEBUQHg=="
    private val playlistsParams = "EgWKAQIoAWoQEAMQBBAJEAoQERAQEBUQHg=="
    private val jsonMedia = "application/json".toMediaType()
    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    suspend fun searchSongs(query: String, maxResults: Int = 20): List<OnlineTrack> = withContext(Dispatchers.IO) {
        val root = search(query, songsParams) ?: return@withContext emptyList()
        parseSongs(root).take(maxResults)
    }

    suspend fun searchAlbums(query: String, maxResults: Int = 12): List<OnlineAlbumResult> = withContext(Dispatchers.IO) {
        val root = search(query, albumsParams) ?: return@withContext emptyList()
        parseAlbums(root).take(maxResults)
    }

    suspend fun searchPlaylists(query: String, maxResults: Int = 12): List<OnlinePlaylistResult> = withContext(Dispatchers.IO) {
        val root = search(query, playlistsParams) ?: return@withContext emptyList()
        parsePlaylists(root).take(maxResults)
    }

    private fun search(query: String, params: String): JSONObject? {
        if (query.isBlank()) return null
        val body = JSONObject().apply {
            put("query", query.trim())
            put("params", params)
            put("context", JSONObject().put("client", JSONObject().apply {
                put("clientName", "WEB_REMIX")
                put("clientVersion", "1.20240501.01.00")
                put("hl", "en")
                put("gl", "US")
                put("utcOffsetMinutes", 0)
            }))
        }.toString()
        val req = Request.Builder()
            .url(endpoint)
            .post(body.toRequestBody(jsonMedia))
            .header("User-Agent", userAgent)
            .header("X-Youtube-Client-Name", "67")
            .header("X-Youtube-Client-Version", "1.20240501.01.00")
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com/")
            .build()
        return runCatching {
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) null else JSONObject(resp.body?.string().orEmpty())
            }
        }.getOrNull()
    }

    private fun parseSongs(root: JSONObject): List<OnlineTrack> {
        val items = musicItems(root)
        return items.mapNotNull { item ->
            val endpoint = firstWatchEndpoint(item) ?: return@mapNotNull null
            val videoId = endpoint.optString("videoId").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val columns = item.optJSONArray("flexColumns") ?: return@mapNotNull null
            val title = columnText(columns, 0).firstOrNull().orEmpty()
            if (title.isBlank()) return@mapNotNull null
            val secondary = columnText(columns, 1).filter { it != " • " && it.isNotBlank() }
            val artist = secondary.firstOrNull { !it.contains(Regex("\\d+:\\d+")) } ?: secondary.firstOrNull().orEmpty()
            val album = secondary.drop(1).firstOrNull { !it.contains(Regex("\\d+:\\d+")) }.orEmpty()
            OnlineTrack(
                videoId = videoId,
                title = title,
                artist = artist.ifBlank { "Unknown Artist" },
                album = album,
                thumbnailUrl = thumbnail(item),
                durationMs = parseDuration(secondary.lastOrNull { it.contains(Regex("\\d+:\\d+")) })
            )
        }.distinctBy { it.videoId }
    }

    private fun parseAlbums(root: JSONObject): List<OnlineAlbumResult> {
        return musicItems(root).mapNotNull { item ->
            val browse = item.optJSONObject("navigationEndpoint")?.optJSONObject("browseEndpoint")
                ?: item.optJSONObject("doubleTapCommand")?.optJSONObject("browseEndpoint")
                ?: return@mapNotNull null
            val browseId = browse.optString("browseId")
            if (!browseId.startsWith("MPRE")) return@mapNotNull null
            val playlistId = findPlaylistId(item) ?: return@mapNotNull null
            val columns = item.optJSONArray("flexColumns") ?: return@mapNotNull null
            val title = columnText(columns, 0).firstOrNull().orEmpty()
            val parts = columnText(columns, 1).filter { it != " • " && it.isNotBlank() }
            OnlineAlbumResult(
                browseId = browseId,
                audioPlaylistId = playlistId,
                title = title,
                artist = parts.getOrNull(1) ?: parts.firstOrNull().orEmpty(),
                year = parts.lastOrNull { it.matches(Regex("\\d{4}")) }.orEmpty(),
                thumbnailUrl = thumbnail(item)
            )
        }.distinctBy { it.audioPlaylistId }
    }

    private fun parsePlaylists(root: JSONObject): List<OnlinePlaylistResult> {
        return musicItems(root).mapNotNull { item ->
            val browseId = item.optJSONObject("navigationEndpoint")?.optJSONObject("browseEndpoint")?.optString("browseId").orEmpty()
            if (!browseId.startsWith("VL")) return@mapNotNull null
            val playlistId = browseId.removePrefix("VL")
            val columns = item.optJSONArray("flexColumns") ?: return@mapNotNull null
            val title = columnText(columns, 0).firstOrNull().orEmpty()
            val parts = columnText(columns, 1).filter { it != " • " && it.isNotBlank() }
            val count = parts.firstOrNull { it.contains("track", true) || it.contains("song", true) }
                ?.let { Regex("(\\d+)").find(it)?.value?.toIntOrNull() } ?: -1
            OnlinePlaylistResult(
                playlistId = playlistId,
                title = title,
                author = parts.getOrNull(1) ?: parts.firstOrNull().orEmpty(),
                trackCount = count,
                thumbnailUrl = thumbnail(item)
            )
        }.distinctBy { it.playlistId }
    }

    private fun musicItems(root: JSONObject): List<JSONObject> {
        val out = ArrayList<JSONObject>()
        fun walk(v: Any?) {
            when (v) {
                is JSONObject -> {
                    v.optJSONObject("musicResponsiveListItemRenderer")?.let(out::add)
                    v.keys().forEach { walk(v.opt(it)) }
                }
                is JSONArray -> for (i in 0 until v.length()) walk(v.opt(i))
            }
        }
        walk(root)
        return out
    }

    private fun columnText(columns: JSONArray, index: Int): List<String> {
        val runs = columns.optJSONObject(index)
            ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
            ?.optJSONObject("text")?.optJSONArray("runs") ?: return emptyList()
        return (0 until runs.length()).mapNotNull { runs.optJSONObject(it)?.optString("text") }
    }

    private fun firstWatchEndpoint(root: JSONObject): JSONObject? {
        fun walk(v: Any?): JSONObject? {
            when (v) {
                is JSONObject -> {
                    v.optJSONObject("watchEndpoint")?.let { return it }
                    v.keys().forEach { key -> walk(v.opt(key))?.let { return it } }
                }
                is JSONArray -> for (i in 0 until v.length()) walk(v.opt(i))?.let { return it }
            }
            return null
        }
        return walk(root)
    }

    private fun findPlaylistId(root: JSONObject): String? {
        fun walk(v: Any?): String? {
            when (v) {
                is JSONObject -> {
                    v.optJSONObject("watchPlaylistEndpoint")?.optString("playlistId")?.takeIf { it.isNotBlank() }?.let { return it }
                    v.optJSONObject("queueTarget")?.optString("playlistId")?.takeIf { it.isNotBlank() }?.let { return it }
                    v.keys().forEach { key -> walk(v.opt(key))?.let { return it } }
                }
                is JSONArray -> for (i in 0 until v.length()) walk(v.opt(i))?.let { return it }
            }
            return null
        }
        return walk(root)
    }

    private fun thumbnail(item: JSONObject): String? {
        val arr = item.optJSONObject("thumbnail")
            ?.optJSONObject("musicThumbnailRenderer")?.optJSONObject("thumbnail")
            ?.optJSONArray("thumbnails") ?: return null
        val raw = if (arr.length() > 0) arr.optJSONObject(arr.length() - 1)?.optString("url") else null
        return raw?.replace(Regex("=w\\d+-h\\d+"), "=w600-h600")
    }

    private fun parseDuration(text: String?): Long {
        if (text.isNullOrBlank()) return 0L
        val parts = text.split(':').mapNotNull { it.toLongOrNull() }
        return when (parts.size) {
            2 -> (parts[0] * 60 + parts[1]) * 1000
            3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000
            else -> 0L
        }
    }
}
