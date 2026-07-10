package com.slowmusic.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.slowmusic.app.data.local.SearchCacheDao
import com.slowmusic.app.data.local.SearchCacheEntity
import com.slowmusic.app.domain.model.Album
import com.slowmusic.app.domain.model.Artist
import com.slowmusic.app.domain.model.Genre
import com.slowmusic.app.domain.model.SearchResult
import com.slowmusic.app.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.contentCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "content_cache")

data class HomeContentSnapshot(
    val trendingSongs: List<Song> = emptyList(),
    val topSongs: List<Song> = emptyList(),
    val recommendations: List<Song> = emptyList(),
    val newReleases: List<Album> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val cachedAt: Long = 0L
)

data class SearchContentSnapshot(
    val results: SearchResult = SearchResult(emptyList(), emptyList(), emptyList(), emptyList()),
    val localSongs: List<Song> = emptyList(),
    val downloadedSongs: List<Song> = emptyList(),
    val cachedAt: Long = 0L
)

@Singleton
class ContentCacheRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val searchCacheDao: SearchCacheDao
) {
    private object Keys {
        val HOME = stringPreferencesKey("home_snapshot")
        val HOME_TIME = longPreferencesKey("home_time")
        val SEARCH_PREFIX = "search_"
    }

    suspend fun getHome(maxAgeMs: Long = HOME_TTL_MS): HomeContentSnapshot? {
        val prefs = context.contentCacheDataStore.data.first()
        val json = prefs[Keys.HOME] ?: return null
        val snapshot = runCatching { gson.fromJson(json, HomeContentSnapshot::class.java) }.getOrNull() ?: return null
        if (snapshot.cachedAt <= 0L) return null
        if (System.currentTimeMillis() - snapshot.cachedAt > maxAgeMs) return null
        return snapshot
    }

    suspend fun saveHome(snapshot: HomeContentSnapshot) {
        val withTime = snapshot.copy(cachedAt = System.currentTimeMillis())
        context.contentCacheDataStore.edit { prefs ->
            prefs[Keys.HOME] = gson.toJson(withTime)
            prefs[Keys.HOME_TIME] = withTime.cachedAt
        }
    }

    suspend fun getSearch(query: String, maxAgeMs: Long = SEARCH_TTL_MS): SearchContentSnapshot? {
        val entity = searchCacheDao.get(normalize(query))
        if (entity != null) {
            if (System.currentTimeMillis() - entity.updatedAt <= maxAgeMs) {
                return runCatching { gson.fromJson(entity.payloadJson, SearchContentSnapshot::class.java) }.getOrNull()
            }
        }
        // Backward-compatible fallback for older installs that had DataStore cache.
        val key = stringPreferencesKey(Keys.SEARCH_PREFIX + normalize(query))
        val json = context.contentCacheDataStore.data.first()[key] ?: return null
        val snapshot = runCatching { gson.fromJson(json, SearchContentSnapshot::class.java) }.getOrNull() ?: return null
        if (snapshot.cachedAt <= 0L) return null
        if (System.currentTimeMillis() - snapshot.cachedAt > maxAgeMs) return null
        return snapshot
    }

    suspend fun saveSearch(query: String, snapshot: SearchContentSnapshot) {
        val withTime = snapshot.copy(cachedAt = System.currentTimeMillis())
        searchCacheDao.put(
            SearchCacheEntity(
                queryKey = normalize(query),
                query = query,
                payloadJson = gson.toJson(withTime),
                updatedAt = withTime.cachedAt
            )
        )
    }

    suspend fun clearHome() {
        context.contentCacheDataStore.edit { it.remove(Keys.HOME); it.remove(Keys.HOME_TIME) }
    }

    suspend fun clearAll() {
        context.contentCacheDataStore.edit { it.clear() }
        searchCacheDao.clear()
    }

    private fun normalize(value: String): String = value.trim().lowercase().replace(Regex("[^a-z0-9_ -]"), "").replace(Regex("\\s+"), "_").take(80)

    companion object {
        const val HOME_TTL_MS = 12L * 60L * 60L * 1000L
        const val SEARCH_TTL_MS = 6L * 60L * 60L * 1000L
    }
}
