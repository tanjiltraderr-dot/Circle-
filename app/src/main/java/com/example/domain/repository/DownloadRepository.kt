package com.example.domain.repository

import com.example.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getAllDownloadedVideos(): Flow<List<Video>>
    suspend fun isVideoDownloaded(videoId: String): Boolean
    suspend fun downloadVideo(video: Video): Result<Unit>
    suspend fun deleteDownloadedVideo(videoId: String)
}
