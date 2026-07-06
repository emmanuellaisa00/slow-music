package com.slowmusic.app.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.slowmusic.app.data.remote.api.ITunesApiService
import com.slowmusic.app.data.repository.*
import com.slowmusic.app.domain.repository.*
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
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideITunesApiService(retrofit: Retrofit): ITunesApiService {
        return retrofit.create(ITunesApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMusicRepository(apiService: ITunesApiService): MusicRepository {
        return MusicRepositoryImpl(apiService)
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
        @ApplicationContext context: Context
    ): SubscriptionRepository {
        return SubscriptionRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideLyricsRepository(): LyricsRepository {
        return LyricsRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideAdRepository(
        @ApplicationContext context: Context
    ): AdRepository {
        return AdRepositoryImpl(context)
    }
}
