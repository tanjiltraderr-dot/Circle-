package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.util.GenesisMatrix
import com.example.data.local.AppDatabase
import com.example.data.remote.SupabaseEdgeApi
import com.example.data.repository.SupabaseVideoRepositoryImpl
import com.example.data.repository.UploadRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.UploadRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.ChannelRepository
import com.example.domain.repository.VideoRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

import com.example.domain.repository.DownloadRepository
import com.example.data.repository.DownloadRepositoryImpl

interface AppContainer {
    val authRepository: AuthRepository
    val videoRepository: VideoRepository
    val uploadRepository: UploadRepository
    val userRepository: UserRepository
    val channelRepository: ChannelRepository
    val downloadRepository: DownloadRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "circle_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    override val authRepository: AuthRepository by lazy {
        com.example.data.repository.SupabaseAuthRepositoryImpl(supabase)
    }

    override val videoRepository: VideoRepository by lazy {
        SupabaseVideoRepositoryImpl(database.videoDao())
    }

    override val userRepository: UserRepository by lazy {
        com.example.data.repository.SupabaseUserRepository(supabase)
    }

    override val channelRepository: ChannelRepository by lazy {
        com.example.data.repository.SupabaseChannelRepositoryImpl(supabase, uploadRepository)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply { 
            level = HttpLoggingInterceptor.Level.BODY 
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GenesisMatrix.endpointUrl + "/") // Add trailing slash just in case
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private val supabaseEdgeApi: SupabaseEdgeApi by lazy {
        retrofit.create(SupabaseEdgeApi::class.java)
    }

    override val uploadRepository: UploadRepository by lazy {
        UploadRepositoryImpl(context, supabaseEdgeApi, okHttpClient)
    }

    override val downloadRepository: DownloadRepository by lazy {
        DownloadRepositoryImpl(context, database.downloadedVideoDao(), okHttpClient)
    }
}

