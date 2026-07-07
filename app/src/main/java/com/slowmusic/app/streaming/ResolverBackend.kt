package com.slowmusic.app.streaming

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolverBackend @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    @Volatile var baseUrl: String? = null

    suspend fun resolve(videoId: String): ResolvedStream? = withContext(Dispatchers.IO) {
        val base = baseUrl?.trim()?.trimEnd('/')?.takeIf { it.startsWith("http") } ?: return@withContext null
        runCatching {
            okHttpClient.newCall(Request.Builder().url("$base/resolve?id=$videoId").build()).execute().use { resp ->
                if (!resp.isSuccessful) return@runCatching null
                val json = JSONObject(resp.body?.string().orEmpty())
                val url = json.optString("url").takeIf { it.isNotBlank() } ?: return@runCatching null
                val headersJson = json.optJSONObject("headers")
                val headers = buildMap {
                    if (headersJson != null) headersJson.keys().forEach { put(it, headersJson.optString(it)) }
                }
                ResolvedStream(url, json.optString("ua").ifBlank { DEFAULT_UA }, headers, "backend")
            }
        }.getOrNull()
    }

    companion object { const val DEFAULT_UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124 Mobile Safari/537.36" }
}
