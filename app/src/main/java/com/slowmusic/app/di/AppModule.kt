package com.slowmusic.app.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import androidx.room.Room
import com.slowmusic.app.data.local.AppDatabase
import com.slowmusic.app.data.local.QueueStateDao
import com.slowmusic.app.data.local.PlayHistoryDao
import com.slowmusic.app.data.local.LyricsCacheDao
import com.slowmusic.app.data.local.SearchCacheDao
import com.slowmusic.app.data.remote.api.LrcLibApiService
import com.slowmusic.app.data.remote.api.LyricsApiService
import com.slowmusic.app.data.repository.*
import com.slowmusic.app.domain.repository.*
import com.slowmusic.app.monetization.BillingManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("lyricsOvh")
    fun provideLyricsOvhRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.lyrics.ovh/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("lrcLib")
    fun provideLrcLibRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideLyricsApiService(@Named("lyricsOvh") retrofit: Retrofit): LyricsApiService {
        return retrofit.create(LyricsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLrcLibApiService(@Named("lrcLib") retrofit: Retrofit): LrcLibApiService {
        return retrofit.create(LrcLibApiService::class.java)
    }
}


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "slow_music.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideQueueStateDao(database: AppDatabase): QueueStateDao = database.queueStateDao()
    @Provides fun providePlayHistoryDao(database: AppDatabase): PlayHistoryDao = database.playHistoryDao()
    @Provides fun provideLyricsCacheDao(database: AppDatabase): LyricsCacheDao = database.lyricsCacheDao()
    @Provides fun provideSearchCacheDao(database: AppDatabase): SearchCacheDao = database.searchCacheDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMusicRepository(
        streamingFallbackResolver: com.slowmusic.app.streaming.StreamingFallbackResolver
    ): MusicRepository {
        return MusicRepositoryImpl(streamingFallbackResolver)
    }

    @Provides
    @Singleton
    fun provideLocalMusicRepository(
        @ApplicationContext context: Context
    ): LocalMusicRepository {
        return LocalMusicRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context,
        gson: Gson
    ): PreferencesRepository {
        return PreferencesRepositoryImpl(context, gson)
    }

    @Provides
    @Singleton
    fun provideLibraryRepository(
        @ApplicationContext context: Context,
        gson: Gson
    ): LibraryRepository {
        return LibraryRepositoryImpl(context, gson)
    }

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        @ApplicationContext context: Context,
        billingManager: BillingManager
    ): SubscriptionRepository {
        return SubscriptionRepositoryImpl(context, billingManager)
    }

    @Provides
    @Singleton
    fun provideLyricsRepository(
        @ApplicationContext context: Context,
        lyricsCacheDao: LyricsCacheDao,
        lyricsApiService: LyricsApiService,
        lrcLibApiService: LrcLibApiService
    ): LyricsRepository {
        return LyricsRepositoryImpl(context, lyricsCacheDao, lyricsApiService, lrcLibApiService)
    }

    @Provides
    @Singleton
    fun provideAdRepository(
        @ApplicationContext context: Context
    ): AdRepository {
        return AdRepositoryImpl(context)
    }
}
