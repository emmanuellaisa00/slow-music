package com.slowmusic.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "queue_state")
data class QueueStateEntity(
    @PrimaryKey val id: String = "active",
    val queueJson: String,
    val currentSongId: String?,
    val currentIndex: Int,
    val positionMs: Long,
    val repeatMode: String,
    val shuffleEnabled: Boolean,
    val updatedAt: Long
)

@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: String,
    val songJson: String,
    val playedAt: Long,
    val source: String? = null
)

@Entity(tableName = "lyrics_cache")
data class LyricsCacheEntity(
    @PrimaryKey val songKey: String,
    val songId: String,
    val lyrics: String,
    val source: String?,
    val updatedAt: Long
)

@Entity(tableName = "search_cache")
data class SearchCacheEntity(
    @PrimaryKey val queryKey: String,
    val query: String,
    val payloadJson: String,
    val updatedAt: Long
)

@Dao
interface QueueStateDao {
    @Query("SELECT * FROM queue_state WHERE id = 'active' LIMIT 1")
    suspend fun getActiveQueue(): QueueStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQueue(state: QueueStateEntity)

    @Query("DELETE FROM queue_state WHERE id = 'active'")
    suspend fun clearQueue()
}

@Dao
interface PlayHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PlayHistoryEntity)

    @Query("SELECT * FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int = 100): List<PlayHistoryEntity>

    @Query("DELETE FROM play_history")
    suspend fun clear()
}

@Dao
interface LyricsCacheDao {
    @Query("SELECT * FROM lyrics_cache WHERE songKey = :songKey LIMIT 1")
    suspend fun get(songKey: String): LyricsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: LyricsCacheEntity)

    @Query("DELETE FROM lyrics_cache")
    suspend fun clear()
}

@Dao
interface SearchCacheDao {
    @Query("SELECT * FROM search_cache WHERE queryKey = :queryKey LIMIT 1")
    suspend fun get(queryKey: String): SearchCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: SearchCacheEntity)

    @Query("DELETE FROM search_cache")
    suspend fun clear()
}

@Database(
    entities = [QueueStateEntity::class, PlayHistoryEntity::class, LyricsCacheEntity::class, SearchCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun queueStateDao(): QueueStateDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun lyricsCacheDao(): LyricsCacheDao
    abstract fun searchCacheDao(): SearchCacheDao
}
