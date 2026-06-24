package com.example.data.repository

import android.content.Context
import com.example.data.local.DownloadedVideoDao
import com.example.data.local.toDomain
import com.example.data.local.toDownloadedEntity
import com.example.domain.model.Video
import com.example.domain.repository.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DownloadRepositoryImpl(
    private val context: Context,
    private val downloadedVideoDao: DownloadedVideoDao,
    private val okHttpClient: OkHttpClient
) : DownloadRepository {

    override fun getAllDownloadedVideos(): Flow<List<Video>> {
        return downloadedVideoDao.getAllDownloadedVideos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun isVideoDownloaded(videoId: String): Boolean {
        return downloadedVideoDao.getDownloadedVideoById(videoId) != null
    }

    override suspend fun downloadVideo(video: Video): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if already downloaded
            if (isVideoDownloaded(video.id)) {
                return@withContext Result.success(Unit)
            }

            // Prepare directory
            val downloadDir = File(context.filesDir, "downloads")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            // Download thumbnail
            val thumbnailFile = File(downloadDir, "thumb_${video.id}.jpg")
            downloadFile(video.thumbnailUrl, thumbnailFile)

            // Download video
            val videoFile = File(downloadDir, "video_${video.id}.mp4")
            downloadFile(video.url, videoFile)

            // Create downloaded entity with local paths
            val downloadedEntity = video.copy(
                thumbnailUrl = "file://" + thumbnailFile.absolutePath,
                localVideoPath = "file://" + videoFile.absolutePath
            ).toDownloadedEntity("file://" + videoFile.absolutePath)

            downloadedVideoDao.insertDownloadedVideo(downloadedEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteDownloadedVideo(videoId: String) = withContext(Dispatchers.IO) {
        val entity = downloadedVideoDao.getDownloadedVideoById(videoId)
        if (entity != null) {
            File(entity.localVideoPath.removePrefix("file://")).delete()
            File(entity.thumbnailUrl.removePrefix("file://")).delete() // if it's local
            downloadedVideoDao.deleteDownloadedVideo(videoId)
        }
    }

    private fun downloadFile(url: String, destFile: File) {
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to download file: $url")
            
            val inputStream: InputStream = response.body?.byteStream() ?: throw Exception("Empty body")
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}
