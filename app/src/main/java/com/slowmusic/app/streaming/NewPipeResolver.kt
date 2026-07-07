package com.slowmusic.app.streaming

import com.slowmusic.app.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as NPRequest
import org.schabi.newpipe.extractor.downloader.Response as NPResponse
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.VideoStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeResolver @Inject constructor() {
    private val initialized = AtomicBoolean(false)
    private val ua = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Mobile Safari/537.36"

    suspend fun resolve(videoId: String): ResolvedStream? = withContext(Dispatchers.IO) {
        ensureInit()
        val info = runCatching { StreamInfo.getInfo(ServiceList.YouTube, "https://www.youtube.com/watch?v=$videoId") }
            .onFailure { Logger.w("NewPipe", "getInfo failed ${it.javaClass.simpleName}: ${it.message}") }
            .getOrNull() ?: return@withContext null
        val audio = pickBestAudio(info.audioStreams)
        val muxed = if (audio == null) pickBestMuxed(info.videoStreams) else null
        val url = audio?.content ?: muxed?.content ?: return@withContext null
        ResolvedStream(url, ua, mapOf("Referer" to "https://www.youtube.com/", "Origin" to "https://www.youtube.com"), "newpipe")
    }

    private fun ensureInit() {
        if (initialized.compareAndSet(false, true)) {
            NewPipe.init(OkHttpDownloader, Localization("en", "US"))
            Logger.d("NewPipe", "initialized")
        }
    }

    private fun pickBestAudio(streams: List<AudioStream>?): AudioStream? = streams.orEmpty()
        .filter { !it.content.isNullOrBlank() }
        .maxByOrNull { it.averageBitrate.takeIf { b -> b > 0 } ?: it.bitrate.takeIf { b -> b > 0 } ?: 0 }

    private fun pickBestMuxed(streams: List<VideoStream>?): VideoStream? = streams.orEmpty()
        .filter { !it.isVideoOnly && !it.content.isNullOrBlank() }
        .maxByOrNull { it.resolution?.takeWhile(Char::isDigit)?.toIntOrNull() ?: 0 }

    private object OkHttpDownloader : Downloader() {
        private val http = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).followRedirects(true).build()
        private const val UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Mobile Safari/537.36"
        override fun execute(request: NPRequest): NPResponse {
            val builder = Request.Builder().url(request.url())
            request.headers().forEach { (name, values) -> values.forEach { builder.addHeader(name, it) } }
            if (request.headers()["User-Agent"].isNullOrEmpty()) builder.header("User-Agent", UA)
            val body = request.dataToSend()
            val method = request.httpMethod()
            if (body != null) {
                val ct = (request.headers()["Content-Type"]?.firstOrNull() ?: "application/octet-stream").toMediaTypeOrNull()
                builder.method(method, body.toRequestBody(ct))
            } else if (method != "GET") {
                builder.method(method, null)
            }
            val resp = http.newCall(builder.build()).execute()
            val respBody = resp.body?.string() ?: ""
            val headers = LinkedHashMap<String, List<String>>()
            resp.headers.names().forEach { name -> headers[name] = resp.headers.values(name) }
            return NPResponse(resp.code, resp.message, headers, respBody, resp.request.url.toString())
        }
    }
}
