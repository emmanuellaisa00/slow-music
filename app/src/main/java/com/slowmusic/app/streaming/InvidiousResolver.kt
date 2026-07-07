package com.slowmusic.app.streaming

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvidiousResolver @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val instances = listOf("https://inv.thepixora.com", "https://invidious.tiekoetter.com")
    private val ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_2 like Mac OS X) AppleWebKit/605.1.15 Version/18.2 Mobile Safari/604.1"

    suspend fun resolve(videoId: String): ResolvedStream? = withContext(Dispatchers.IO) {
        for (base in instances) {
            val stream = runCatching {
                val json = okHttpClient.newCall(Request.Builder().url("$base/api/v1/videos/$videoId?fields=adaptiveFormats,formatStreams,hlsUrl").header("User-Agent", ua).build())
                    .execute().use { resp -> if (!resp.isSuccessful) return@runCatching null else JSONObject(resp.body?.string().orEmpty()) }
                val url = pick(json.optJSONArray("adaptiveFormats"))
                    ?: pick(json.optJSONArray("formatStreams"))
                    ?: json.optString("hlsUrl").takeIf { it.isNotBlank() }
                url?.let { ResolvedStream(it, ua, mapOf("Referer" to "$base/"), "invidious") }
            }.getOrNull()
            if (stream != null) return@withContext stream
        }
        null
    }

    private fun pick(arr: JSONArray?): String? {
        if (arr == null) return null
        var best: String? = null
        var score = -1
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val url = o.optString("url").takeIf { it.isNotBlank() } ?: continue
            val s = o.optInt("bitrate", 0) + o.optInt("fps", 0)
            if (s >= score) { score = s; best = url }
        }
        return best
    }
}
