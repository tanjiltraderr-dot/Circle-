package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE isShort = :isShort ORDER BY uploadDate DESC")
    fun getVideos(isShort: Boolean): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE creatorId = :channelId ORDER BY uploadDate DESC")
    fun getVideosByChannelLatest(channelId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE creatorId = :channelId AND isShort = :isShort ORDER BY uploadDate DESC")
    fun getVideosByChannelLatest(channelId: String, isShort: Boolean): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE creatorId = :channelId AND isShort = :isShort ORDER BY uploadDate ASC")
    fun getVideosByChannelOldest(channelId: String, isShort: Boolean): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE creatorId = :channelId AND isShort = :isShort ORDER BY views DESC")
    fun getVideosByChannelPopular(channelId: String, isShort: Boolean): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Query("UPDATE videos SET title = :title, description = :description, thumbnailUrl = :thumbnailUrl WHERE id = :videoId")
    suspend fun updateVideoMetadata(videoId: String, title: String, description: String, thumbnailUrl: String)

    @Query("DELETE FROM videos WHERE isShort = :isShort")
    suspend fun clearVideos(isShort: Boolean)
}
