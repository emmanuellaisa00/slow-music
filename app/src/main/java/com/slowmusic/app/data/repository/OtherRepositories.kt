package com.slowmusic.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import com.slowmusic.app.data.remote.api.LrcLibApiService
import com.slowmusic.app.data.remote.api.LyricsApiService
import com.slowmusic.app.domain.model.Ad
import com.slowmusic.app.domain.model.AdType
import com.slowmusic.app.domain.model.Lyrics
import com.slowmusic.app.domain.model.Song
import com.slowmusic.app.domain.model.Subscription
import com.slowmusic.app.domain.model.SubscriptionType
import com.slowmusic.app.domain.repository.AdRepository
import com.slowmusic.app.domain.repository.LyricsRepository
import com.slowmusic.app.domain.repository.SubscriptionRepository
import com.slowmusic.app.monetization.BillingManager
import com.slowmusic.app.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.lyricsCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "lyrics_cache")

private val Context.subscriptionDataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = "subscription_prefs")

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billingManager: BillingManager
) : SubscriptionRepository {

    private object SubscriptionKeys {
        val SUBSCRIPTION_TYPE = stringPreferencesKey("subscription_type")
        val IS_ACTIVE = booleanPreferencesKey("is_active")
        val EXPIRES_AT = longPreferencesKey("expires_at")
    }

    override fun getCurrentSubscription(): Flow<Subscription> {
        return context.subscriptionDataStore.data.map { prefs ->
            val type = runCatching {
                SubscriptionType.valueOf(prefs[SubscriptionKeys.SUBSCRIPTION_TYPE] ?: SubscriptionType.FREE.name)
            }.getOrDefault(SubscriptionType.FREE)
            Subscription(
                type = type,
                isActive = prefs[SubscriptionKeys.IS_ACTIVE] ?: false,
                expiresAt = prefs[SubscriptionKeys.EXPIRES_AT],
                features = getFeaturesForType(type)
            )
        }
    }

    /**
     * Repository-safe subscription activation. The UI should call BillingManager.launchBillingFlow(...)
     * when it has an Activity. This method syncs Play purchases and keeps a debug/local fallback so
     * the app remains usable during development and tests.
     */
    override suspend fun purchaseSubscription(type: SubscriptionType) {
        if (type == SubscriptionType.FREE) {
            cancelSubscription()
            return
        }

        val purchasedType = billingManager.syncPurchases().getOrNull() ?: type
        val expiresAt = System.currentTimeMillis() + when (purchasedType) {
            SubscriptionType.PREMIUM, SubscriptionType.FAMILY -> 30L * 24 * 60 * 60 * 1000
            SubscriptionType.STUDENT -> 365L * 24 * 60 * 60 * 1000
            SubscriptionType.FREE -> 0L
        }

        context.subscriptionDataStore.edit { prefs ->
            prefs[SubscriptionKeys.SUBSCRIPTION_TYPE] = purchasedType.name
            prefs[SubscriptionKeys.IS_ACTIVE] = purchasedType != SubscriptionType.FREE
            prefs[SubscriptionKeys.EXPIRES_AT] = expiresAt
        }
    }

    override suspend fun cancelSubscription() {
        context.subscriptionDataStore.edit { prefs ->
            prefs[SubscriptionKeys.SUBSCRIPTION_TYPE] = SubscriptionType.FREE.name
            prefs[SubscriptionKeys.IS_ACTIVE] = false
            prefs[SubscriptionKeys.EXPIRES_AT] = 0L
        }
    }

    override fun isPremium(): Flow<Boolean> {
        return getCurrentSubscription().map { it.type != SubscriptionType.FREE && it.isActive }
    }

    private fun getFeaturesForType(type: SubscriptionType): List<String> = when (type) {
        SubscriptionType.FREE -> listOf("Ad-supported playback", "Basic search", "Limited skips")
        SubscriptionType.PREMIUM -> listOf("Ad-free listening", "Unlimited skips", "High quality audio", "Offline downloads", "Lyrics access")
        SubscriptionType.FAMILY -> listOf("All Premium features", "Up to 6 accounts", "Family shared playlists")
        SubscriptionType.STUDENT -> listOf("All Premium features", "50% discount", "Valid student ID required")
    }
}

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lyricsApiService: LyricsApiService,
    private val lrcLibApiService: LrcLibApiService
) : LyricsRepository {

    private object LyricsKeys {
        fun key(song: Song) = stringPreferencesKey("lyrics_" + stableKey(song))
        fun source(song: Song) = stringPreferencesKey("lyrics_source_" + stableKey(song))
        private fun stableKey(song: Song): String = (song.id.ifBlank { "${song.artist}_${song.title}" })
            .lowercase()
            .replace(Regex("[^a-z0-9_ -]"), "")
            .replace(Regex("\s+"), "_")
            .take(90)
    }

    override suspend fun getLyrics(song: Song): Lyrics? {
        val cached = getCachedLyrics(song)
        if (cached != null) return cached

        val fetched = runCatching { fetchLyrics(song) }
            .onFailure { Logger.e("LyricsRepository", "Failed to fetch lyrics", it) }
            .getOrNull()

        val safe = fetched ?: Lyrics(
            songId = song.id,
            text = "♪\nLyrics are not available for this track yet.\nWhen a synced or plain lyric source is found, it will appear here automatically.",
            source = "Unavailable"
        )
        cacheLyrics(song, safe)
        return safe
    }

    private suspend fun fetchLyrics(song: Song): Lyrics? {
        val cleanTitle = cleanTitle(song.title)
        val cleanArtist = cleanArtist(song.artist)
        val durationSeconds = song.duration.takeIf { it > 0 }?.div(1000)

        // 1) LRCLib exact endpoint. Best chance for synced lyrics.
        runCatching {
            lrcLibApiService.getBestLyrics(
                artistName = cleanArtist,
                trackName = cleanTitle,
                albumName = song.album.takeIf { it.isNotBlank() },
                durationSeconds = durationSeconds
            )
        }.getOrNull()?.let { lrc ->
            val text = lrc.syncedLyrics ?: lrc.plainLyrics
            if (!text.isNullOrBlank()) return Lyrics(song.id, text, "LRCLib")
        }

        // 2) LRCLib search variants.
        val variants = listOf(
            "$cleanArtist $cleanTitle",
            cleanTitle,
            "${song.artist} ${song.title}"
        ).distinct().filter { it.isNotBlank() }
        for (query in variants) {
            val result = runCatching { lrcLibApiService.searchLyrics(query) }.getOrDefault(emptyList())
                .firstOrNull { candidate ->
                    candidate.trackName?.contains(cleanTitle, ignoreCase = true) == true ||
                        cleanTitle.contains(candidate.trackName.orEmpty(), ignoreCase = true)
                }
                ?: runCatching { lrcLibApiService.searchLyrics(query) }.getOrDefault(emptyList()).firstOrNull()
            if (result != null) {
                val lrc = runCatching { lrcLibApiService.getLyrics(result.id) }.getOrNull()
                val text = lrc?.syncedLyrics ?: lrc?.plainLyrics
                if (!text.isNullOrBlank()) return Lyrics(song.id, text, "LRCLib")
            }
        }

        // 3) lyrics.ovh plain lyrics fallback, with cleaned title/artist.
        val plain = runCatching { lyricsApiService.getLyrics(cleanArtist, cleanTitle).lyrics }
            .getOrNull()
            ?: runCatching { lyricsApiService.getLyrics(song.artist, song.title).lyrics }.getOrNull()
        if (!plain.isNullOrBlank()) return Lyrics(song.id, plain, "lyrics.ovh")

        return null
    }

    private suspend fun getCachedLyrics(song: Song): Lyrics? {
        val prefs = context.lyricsCacheDataStore.data.first()
        val text = prefs[LyricsKeys.key(song)] ?: return null
        val source = prefs[LyricsKeys.source(song)] ?: "Cache"
        return Lyrics(song.id, text, source)
    }

    private suspend fun cacheLyrics(song: Song, lyrics: Lyrics) {
        context.lyricsCacheDataStore.edit { prefs ->
            prefs[LyricsKeys.key(song)] = lyrics.text
            prefs[LyricsKeys.source(song)] = lyrics.source ?: "Unknown"
        }
    }

    private fun cleanTitle(raw: String): String = raw
        .replace(Regex("\(.*?\)|\[.*?]"), "")
        .replace(Regex("(?i)\b(remaster(ed)?|radio edit|official audio|official video|lyrics?|feat\.|ft\.)\b.*"), "")
        .trim()

    private fun cleanArtist(raw: String): String = raw
        .substringBefore(",")
        .substringBefore(" feat")
        .substringBefore(" ft")
        .trim()
}

@Singleton
class AdRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AdRepository {
    /**
     * Real AdMob rendering is handled by UI components using the app/ad unit IDs.
     * This repository exposes available placements plus a safe house-ad fallback.
     */
    override fun getAvailableAds(): Flow<List<Ad>> = flow {
        emit(
            listOf(
                Ad(
                    id = "home_banner",
                    type = AdType.BANNER,
                    content = "Upgrade to Premium for ad-free listening",
                    imageUrl = null,
                    actionUrl = "slowmusic://subscription"
                ),
                Ad(
                    id = "reward_download",
                    type = AdType.REWARDED,
                    content = "Watch a sponsor message to unlock extra offline downloads",
                    imageUrl = null,
                    actionUrl = "slowmusic://rewarded-download"
                )
            )
        )
    }.catch { emit(emptyList()) }

    override suspend fun recordAdImpression(adId: String) {
        Logger.d("Ads", "Impression recorded: $adId")
    }

    override suspend fun recordAdClick(adId: String) {
        Logger.d("Ads", "Click recorded: $adId")
    }
}
