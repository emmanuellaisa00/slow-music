package com.slowmusic.app.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.slowmusic.app.domain.model.RepeatMode
import com.slowmusic.app.domain.model.Song
import javax.inject.Inject
import javax.inject.Singleton

data class SavedQueueState(
    val queue: List<Song>,
    val currentSongId: String?,
    val currentIndex: Int,
    val positionMs: Long,
    val repeatMode: RepeatMode,
    val shuffleEnabled: Boolean
)

@Singleton
class QueueStateRepository @Inject constructor(
    private val queueStateDao: QueueStateDao,
    private val playHistoryDao: PlayHistoryDao,
    private val gson: Gson
) {
    suspend fun save(state: SavedQueueState) {
        queueStateDao.saveQueue(
            QueueStateEntity(
                queueJson = gson.toJson(state.queue),
                currentSongId = state.currentSongId,
                currentIndex = state.currentIndex,
                positionMs = state.positionMs,
                repeatMode = state.repeatMode.name,
                shuffleEnabled = state.shuffleEnabled,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun restore(): SavedQueueState? {
        val entity = queueStateDao.getActiveQueue() ?: return null
        val type = object : TypeToken<List<Song>>() {}.type
        val queue: List<Song> = runCatching { gson.fromJson<List<Song>>(entity.queueJson, type) }.getOrDefault(emptyList())
        if (queue.isEmpty()) return null
        return SavedQueueState(
            queue = queue,
            currentSongId = entity.currentSongId,
            currentIndex = entity.currentIndex.coerceIn(0, queue.lastIndex),
            positionMs = entity.positionMs,
            repeatMode = runCatching { RepeatMode.valueOf(entity.repeatMode) }.getOrDefault(RepeatMode.OFF),
            shuffleEnabled = entity.shuffleEnabled
        )
    }

    suspend fun clear() = queueStateDao.clearQueue()

    suspend fun recordPlay(song: Song, source: String? = null) {
        playHistoryDao.insert(
            PlayHistoryEntity(
                songId = song.id,
                songJson = gson.toJson(song),
                playedAt = System.currentTimeMillis(),
                source = source
            )
        )
    }
}
