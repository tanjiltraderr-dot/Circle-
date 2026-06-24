package com.example.domain.repository

import com.example.domain.model.Video

interface UserRepository {
    suspend fun getWatchHistory(): List<Video>
    suspend fun addToWatchHistory(video: Video)
    suspend fun getLikedVideos(): List<Video>
    suspend fun getSavedVideos(): List<Video>
}
