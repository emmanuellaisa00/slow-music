package com.slowmusic.app.streaming

import android.net.Uri
import com.slowmusic.app.domain.model.Album
import com.slowmusic.app.domain.model.Playlist
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingFallbackResolver @Inject constructor(
    private val youtubeMusicSearch: YoutubeMusicSearch,
    private val resolverBackend: ResolverBackend,
    private val pipedResolver: PipedResolver,
    private val newPipeResolver: NewPipeResolver,
    private val innertubeResolver: InnertubeResolver,
    private val webViewStreamResolver: WebViewStreamResolver,
    private val invidiousResolver: InvidiousResolver
) {
    private data class CacheEntry(val stream: ResolvedStream, val timeMs: Long)
    private val streamCache = ConcurrentHashMap<String, CacheEntry>()
    private val ttlMs = 90L * 60L * 1000L

    suspend fun searchSongs(query: String, maxResults: Int = 20): List<Song> =
        youtubeMusicSearch.searchSongs(query, maxResults).map { it.toSong() }

    suspend fun searchAlbums(query: String, maxResults: Int = 12): List<Album> =
        youtubeMusicSearch.searchAlbums(query, maxResults).map { it.toAlbum() }

    suspend fun searchPlaylists(query: String, maxResults: Int = 12): List<Playlist> =
        youtubeMusicSearch.searchPlaylists(query, maxResults).map { it.toPlaylist() }

    suspend fun playlistSongs(playlistId: String): List<Song> {
        val clean = playlistId.removePrefix("ytpl_").removePrefix("ytalbum_")
        return pipedResolver.playlistTracks(clean).map { it.toSong() }
    }

    suspend fun resolveSong(song: Song): ResolvedStream? = withContext(Dispatchers.IO) {
        val videoId = videoIdFor(song) ?: run {
            val candidate = youtubeMusicSearch.searchSongs("${song.title} ${song.artist}", 1).firstOrNull()
            candidate?.videoId
        } ?: return@withContext null
        resolveVideoId(videoId)
    }

    suspend fun resolveVideoId(videoId: String): ResolvedStream? = withContext(Dispatchers.IO) {
        getCached(videoId)?.let { return@withContext it }
        val stream = resolverBackend.resolve(videoId)
            ?: pipedResolver.resolve(videoId)
            ?: newPipeResolver.resolve(videoId)
            ?: innertubeResolver.resolve(videoId)
            ?: webViewStreamResolver.resolve(videoId)
            ?: pipedResolver.resolveExhaustive(videoId)
            ?: invidiousResolver.resolve(videoId)
        if (stream != null) {
            streamCache[videoId] = CacheEntry(stream, System.currentTimeMillis())
            Logger.d("StreamingFallback", "Resolved $videoId via ${stream.source}")
        } else {
            Logger.w("StreamingFallback", "All fallback resolvers failed for $videoId")
        }
        stream
    }

    fun invalidate(song: Song) {
        videoIdFor(song)?.let(streamCache::remove)
    }

    fun invalidateVideoId(videoId: String) { streamCache.remove(videoId) }

    fun encodeStreamUrl(stream: ResolvedStream): String {
        val headers = LinkedHashMap<String, String>()
        if (stream.userAgent.isNotBlank()) headers[StreamHeaderCodec.userAgentKey()] = stream.userAgent
        headers.putAll(stream.headers)
        val fragment = StreamHeaderCodec.encode(headers)
        return if (fragment.isBlank()) stream.url else Uri.parse(stream.url).buildUpon().fragment(fragment).build().toString()
    }

    private fun getCached(videoId: String): ResolvedStream? {
        val entry = streamCache[videoId] ?: return null
        return if (System.currentTimeMillis() - entry.timeMs < ttlMs) entry.stream else {
            streamCache.remove(videoId); null
        }
    }

    private fun videoIdFor(song: Song): String? {
        val raw = song.id.removePrefix("yt_").removePrefix("youtube_")
        if (raw.length == 11 && raw.all { it.isLetterOrDigit() || it == '_' || it == '-' }) return raw
        listOf(song.streamUrl, song.previewUrl).forEach { url ->
            val parsed = runCatching { Uri.parse(url) }.getOrNull() ?: return@forEach
            parsed.getQueryParameter("v")?.takeIf { it.length == 11 }?.let { return it }
            parsed.lastPathSegment?.takeIf { it.length == 11 }?.let { return it }
        }
        return null
    }
}
