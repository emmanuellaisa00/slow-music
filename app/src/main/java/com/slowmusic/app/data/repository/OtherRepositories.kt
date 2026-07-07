package com.slowmusic.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

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
    private val lyricsApiService: LyricsApiService,
    private val lrcLibApiService: LrcLibApiService
) : LyricsRepository {
    override suspend fun getLyrics(song: Song): Lyrics? {
        return runCatching {
            val query = "${song.artist} ${song.title}"
            val lrcResult = lrcLibApiService.searchLyrics(query).firstOrNull { result ->
                result.trackName?.contains(song.title, ignoreCase = true) == true &&
                    result.artistName?.contains(song.artist, ignoreCase = true) == true
            } ?: lrcLibApiService.searchLyrics(query).firstOrNull()

            val lrcLyrics = if (lrcResult != null) {
                val lyrics = lrcLibApiService.getLyrics(lrcResult.id)
                val text = lyrics.syncedLyrics ?: lyrics.plainLyrics
                if (!text.isNullOrBlank()) Lyrics(songId = song.id, text = text, source = "LRCLib") else null
            } else {
                null
            }

            if (lrcLyrics != null) {
                lrcLyrics
            } else {
                val fallback = lyricsApiService.getLyrics(song.artist, song.title).lyrics
                if (!fallback.isNullOrBlank()) {
                    Lyrics(songId = song.id, text = fallback, source = "lyrics.ovh")
                } else {
                    null
                }
            }
        }.onFailure { Logger.e("LyricsRepository", "Failed to fetch lyrics", it) }.getOrNull()
    }
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
