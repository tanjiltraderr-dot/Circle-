package com.example.data.repository

import com.example.domain.model.Video
import com.example.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SupabaseVideoDto(
    val id: String,
    @SerialName("channel_id") val channelId: String,
    val title: String,
    val description: String? = null,
    val url: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("is_short") val isShort: Boolean = false,
    @SerialName("is_downloadable") val isDownloadable: Boolean = true,
    @SerialName("views_count") val viewsCount: Int = 0,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("comments_count") val commentsCount: Int = 0,
    @SerialName("created_at") val createdAt: String = ""
) {
    fun toDomain(): Video {
        return Video(
            id = id,
            title = title,
            description = description ?: "",
            url = url,
            thumbnailUrl = thumbnailUrl ?: "",
            creatorId = channelId,
            creatorName = "Channel",
            creatorProfilePic = null,
            isShort = isShort,
            likes = likesCount,
            views = viewsCount,
            commentsCount = commentsCount,
            category = "Unknown",
            isDownloadable = isDownloadable
        )
    }
}

@Serializable
data class WatchHistoryDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("video_id") val videoId: String,
    @SerialName("watched_at") val watchedAt: String
)

@Serializable
data class LikeDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("video_id") val videoId: String
)

@Serializable
data class SavedVideoDto(
    @SerialName("user_id") val userId: String,
    @SerialName("video_id") val videoId: String
)

class SupabaseUserRepository(
    private val supabase: SupabaseClient
) : UserRepository {

    override suspend fun getWatchHistory(): List<Video> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
            // In a real app we'd join, but let's fetch history then videos
            val history = supabase.postgrest["watch_history"]
                .select {
                    filter { eq("user_id", userId) }
                    order("watched_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(50)
                }.decodeList<WatchHistoryDto>()
            
            if (history.isEmpty()) return@withContext emptyList()
            
            val videoIds = history.map { it.videoId }.distinct()
            val videos = supabase.postgrest["videos"]
                .select {
                    filter { isIn("id", videoIds) }
                }.decodeList<SupabaseVideoDto>()
                
            val videoMap = videos.associateBy { it.id }
            // return sorted by history
            history.mapNotNull { videoMap[it.videoId]?.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun addToWatchHistory(video: Video) = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext
            supabase.postgrest["watch_history"].insert(
                WatchHistoryDto(
                    userId = userId,
                    videoId = video.id,
                    watchedAt = java.time.Instant.now().toString()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getLikedVideos(): List<Video> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
            val likes = supabase.postgrest["likes"]
                .select {
                    filter { 
                        eq("user_id", userId) 
                        neq("video_id", "null") 
                    }
                }.decodeList<LikeDto>()
                
            if (likes.isEmpty()) return@withContext emptyList()
            
            val videoIds = likes.map { it.videoId }.distinct()
            val videos = supabase.postgrest["videos"]
                .select {
                    filter { isIn("id", videoIds) }
                }.decodeList<SupabaseVideoDto>()
                
            videos.map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getSavedVideos(): List<Video> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
            val saved = supabase.postgrest["saved_videos"]
                .select {
                    filter { eq("user_id", userId) }
                }.decodeList<SavedVideoDto>()
                
            if (saved.isEmpty()) return@withContext emptyList()
            
            val videoIds = saved.map { it.videoId }.distinct()
            val videos = supabase.postgrest["videos"]
                .select {
                    filter { isIn("id", videoIds) }
                }.decodeList<SupabaseVideoDto>()
                
            videos.map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
