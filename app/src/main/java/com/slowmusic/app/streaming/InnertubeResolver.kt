package com.slowmusic.app.streaming

import com.slowmusic.app.util.Logger
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
class InnertubeResolver @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val mediaType = "application/json".toMediaType()
    private val clients = listOf(
        YtClient("ANDROID_TESTSUITE", "ANDROID_TESTSUITE", "1.9", mapOf(
            "User-Agent" to "com.google.android.youtube/1.9 (Linux; U; Android 14; Pixel 8 Pro) gzip",
            "X-Youtube-Client-Name" to "30",
            "X-Youtube-Client-Version" to "1.9"
        )),
        YtClient("ANDROID", "ANDROID", "20.10.38", mapOf(
            "User-Agent" to "com.google.android.youtube/20.10.38 (Linux; U; Android 14; Pixel 8 Pro) gzip",
            "X-Youtube-Client-Name" to "3",
            "X-Youtube-Client-Version" to "20.10.38"
        )),
        YtClient("IOS", "IOS", "20.10.04", mapOf(
            "User-Agent" to "com.google.ios.youtube/20.10.04 (iPhone16,2; U; CPU iOS 18_2_1 like Mac OS X;)",
            "X-Youtube-Client-Name" to "5",
            "X-Youtube-Client-Version" to "20.10.04",
            "Origin" to "https://www.youtube.com",
            "Referer" to "https://www.youtube.com/"
        )),
        YtClient("MWEB", "MWEB", "2.20250101.00", mapOf(
            "User-Agent" to "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 Version/17.0 Mobile Safari/604.1",
            "X-Youtube-Client-Name" to "2",
            "X-Youtube-Client-Version" to "2.20250101.00",
            "Origin" to "https://m.youtube.com",
            "Referer" to "https://m.youtube.com/"
        ))
    )

    suspend fun resolve(videoId: String, allowMuxed: Boolean = true): ResolvedStream? = withContext(Dispatchers.IO) {
        YoutubeCipher.discoverPlayerJsUrlCached()?.let { runCatching { YoutubeCipher.ensurePlayer(it) } }
        for (client in clients) {
            val result = runCatching { resolveWithClient(videoId, client, allowMuxed) }
                .onFailure { Logger.w("Innertube", "${client.name} failed: ${it.message}") }
                .getOrNull()
            if (result != null) return@withContext result
        }
        null
    }

    private suspend fun resolveWithClient(videoId: String, client: YtClient, allowMuxed: Boolean): ResolvedStream? {
        val body = JSONObject().apply {
            put("videoId", videoId)
            put("context", JSONObject().put("client", JSONObject().apply {
                put("clientName", client.clientName)
                put("clientVersion", client.clientVersion)
                put("hl", "en")
                put("gl", "US")
                put("utcOffsetMinutes", 0)
            }))
        }.toString()
        val req = Request.Builder()
            .url("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
            .post(body.toRequestBody(mediaType))
            .apply { client.headers.forEach { (k, v) -> header(k, v) } }
            .build()
        val root = okHttpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            JSONObject(resp.body?.string().orEmpty())
        }
        val status = root.optJSONObject("playabilityStatus")?.optString("status").orEmpty()
        if (status.equals("LOGIN_REQUIRED", true) || status.equals("UNPLAYABLE", true)) return null
        val sd = root.optJSONObject("streamingData") ?: return null
        val audioUrl = pickBest(sd.optJSONArray("adaptiveFormats"), audioOnly = true)
        val muxedUrl = if (audioUrl == null && allowMuxed) pickBest(sd.optJSONArray("formats"), audioOnly = false) else null
        val hls = sd.optString("hlsManifestUrl").takeIf { it.isNotBlank() }
        val url = audioUrl ?: muxedUrl ?: hls ?: return null
        val ua = client.headers["User-Agent"] ?: DEFAULT_UA
        val extra = client.headers.filterKeys { it != "User-Agent" }
        return ResolvedStream(url, ua, extra, "innertube-${client.name}")
    }

    private suspend fun pickBest(arr: JSONArray?, audioOnly: Boolean): String? {
        if (arr == null) return null
        var bestUrl: String? = null
        var bestScore = -1
        for (i in 0 until arr.length()) {
            val f = arr.optJSONObject(i) ?: continue
            val mime = f.optString("mimeType")
            if (audioOnly && !mime.startsWith("audio/")) continue
            if (!audioOnly && mime.startsWith("audio/")) continue
            val url = YoutubeCipher.resolveFormatUrl(f) ?: continue
            val score = f.optInt("bitrate", 0)
            if (score >= bestScore) { bestScore = score; bestUrl = url }
        }
        return bestUrl
    }

    private data class YtClient(val name: String, val clientName: String, val clientVersion: String, val headers: Map<String, String>)
    companion object { const val DEFAULT_UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124 Mobile Safari/537.36" }
}
