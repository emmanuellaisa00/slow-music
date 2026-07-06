package com.slowmusic.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.domain.repository.AdRepository
import com.slowmusic.app.domain.repository.LyricsRepository
import com.slowmusic.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.subscriptionDataStore: DataStore<Preferences> by preferencesDataStore(name = "subscription_prefs")

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SubscriptionRepository {

    private object SubscriptionKeys {
        val SUBSCRIPTION_TYPE = stringPreferencesKey("subscription_type")
        val IS_ACTIVE = booleanPreferencesKey("is_active")
        val EXPIRES_AT = longPreferencesKey("expires_at")
    }

    override fun getCurrentSubscription(): Flow<Subscription> {
        return context.subscriptionDataStore.data.map { prefs ->
            Subscription(
                type = SubscriptionType.valueOf(
                    prefs[SubscriptionKeys.SUBSCRIPTION_TYPE] ?: SubscriptionType.FREE.name
                ),
                isActive = prefs[SubscriptionKeys.IS_ACTIVE] ?: false,
                expiresAt = prefs[SubscriptionKeys.EXPIRES_AT],
                features = getFeaturesForType(
                    SubscriptionType.valueOf(
                        prefs[SubscriptionKeys.SUBSCRIPTION_TYPE] ?: SubscriptionType.FREE.name
                    )
                )
            )
        }
    }

    override suspend fun purchaseSubscription(type: SubscriptionType) {
        val expiresAt = System.currentTimeMillis() + when (type) {
            SubscriptionType.PREMIUM -> 30L * 24 * 60 * 60 * 1000 // 30 days
            SubscriptionType.FAMILY -> 30L * 24 * 60 * 60 * 1000 // 30 days
            SubscriptionType.STUDENT -> 365L * 24 * 60 * 60 * 1000 // 1 year
            SubscriptionType.FREE -> 0L
        }
        
        context.subscriptionDataStore.edit { prefs ->
            prefs[SubscriptionKeys.SUBSCRIPTION_TYPE] = type.name
            prefs[SubscriptionKeys.IS_ACTIVE] = type != SubscriptionType.FREE
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
        return context.subscriptionDataStore.data.map { prefs ->
            val type = SubscriptionType.valueOf(
                prefs[SubscriptionKeys.SUBSCRIPTION_TYPE] ?: SubscriptionType.FREE.name
            )
            type != SubscriptionType.FREE && (prefs[SubscriptionKeys.IS_ACTIVE] ?: false)
        }
    }

    private fun getFeaturesForType(type: SubscriptionType): List<String> {
        return when (type) {
            SubscriptionType.FREE -> listOf(
                "Ad-supported playback",
                "Basic search",
                "Limited skips"
            )
            SubscriptionType.PREMIUM -> listOf(
                "Ad-free listening",
                "Unlimited skips",
                "High quality audio",
                "Offline downloads",
                "Lyrics access"
            )
            SubscriptionType.FAMILY -> listOf(
                "All Premium features",
                "Up to 6 accounts",
                "Family shared playlists"
            )
            SubscriptionType.STUDENT -> listOf(
                "All Premium features",
                "50% discount",
                "Valid student ID required"
            )
        }
    }
}

@Singleton
class LyricsRepositoryImpl @Inject constructor() : LyricsRepository {
    
    // Simple mock lyrics - in production, you'd use a real lyrics API
    override suspend fun getLyrics(song: Song): Lyrics? {
        // Return null for now - would integrate with a lyrics API like
        // Musixmatch, Genius, or LRCLib
        return null
    }
}

@Singleton
class AdRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AdRepository {
    
    // Mock ads - would integrate with AdMob or similar
    override fun getAvailableAds(): Flow<List<Ad>> {
        return kotlinx.coroutines.flow.flow {
            emit(emptyList()) // No ads for now
        }
    }

    override suspend fun recordAdImpression(adId: String) {
        // Record impression analytics
    }

    override suspend fun recordAdClick(adId: String) {
        // Record click analytics
    }
}
