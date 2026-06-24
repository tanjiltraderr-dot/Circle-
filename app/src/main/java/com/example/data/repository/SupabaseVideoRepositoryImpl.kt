package com.example.data.repository

import com.example.data.local.VideoDao
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.domain.model.Category
import com.example.domain.model.Comment
import com.example.domain.model.Video
import com.example.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import io.github.jan.supabase.postgrest.postgrest
import com.example.di.supabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelInfoDto(
    val name: String,
    @SerialName("logo_url") val logoUrl: String? = null
)

@Serializable
data class CategoryInfoDto(
    val name: String
)

@Serializable
data class VideoDto(
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
    @SerialName("created_at") val createdAt: String? = null,
    val channels: ChannelInfoDto? = null,
    val categories: CategoryInfoDto? = null
) {
    fun toDomain(): Video {
        return Video(
            id = id,
            title = title,
            description = description ?: "",
            url = url,
            thumbnailUrl = thumbnailUrl ?: "",
            creatorId = channelId,
            creatorName = channels?.name ?: "Unknown",
            creatorProfilePic = channels?.logoUrl,
            isShort = isShort,
            likes = likesCount,
            views = viewsCount,
            commentsCount = commentsCount,
            category = categories?.name ?: "",
            isDownloadable = isDownloadable
        )
    }
}

@Serializable
data class CategoryDto(
    val id: String,
    val name: String
)

@Serializable
data class UserInfoDto(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("profile_picture") val profilePicture: String? = null
)

@Serializable
data class CommentDto(
    val id: String,
    @SerialName("video_id") val videoId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("parent_id") val parentId: String? = null,
    val content: String,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    val users: UserInfoDto? = null
) {
    fun toDomain(): Comment {
        return Comment(
            id = id,
            videoId = videoId,
            userId = userId,
            userName = users?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
            userProfilePic = users?.profilePicture,
            content = content,
            likes = likesCount
        )
    }
}

class SupabaseVideoRepositoryImpl(
    private val dao: VideoDao
) : VideoRepository {

    override fun getHomeFeed(isShort: Boolean): Flow<List<Video>> = flow {
        try {
            val videos = supabase.postgrest["videos"]
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, channels(name, logo_url), categories(name)")) {
                    filter { eq("is_short", isShort) }
                }
                .decodeList<VideoDto>()
            emit(videos.map { it.toDomain() }.sortedByDescending { it.uploadDate })
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override fun getCategories(): Flow<List<Category>> = flow {
        try {
            val categories = supabase.postgrest["categories"]
                .select().decodeList<CategoryDto>()
            emit(categories.map { Category(it.id, it.name) })
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override suspend fun getComments(videoId: String): Flow<List<Comment>> = flow {
        try {
            val comments = supabase.postgrest["comments"]
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, users(first_name, last_name, profile_picture)")) {
                    filter { eq("video_id", videoId) }
                }.decodeList<CommentDto>()
            
            // Build simple hierarchy for replies if any
            val domains = comments.map { it.toDomain() }
            val rootComments = domains.filter { c -> comments.find { it.id == c.id }?.parentId == null }
            val replies = domains.filter { c -> comments.find { it.id == c.id }?.parentId != null }
            
            val finalComments = rootComments.map { root ->
                root.copy(replies = replies.filter { r -> 
                    comments.find { it.id == r.id }?.parentId == root.id 
                })
            }
            emit(finalComments)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override fun getChannelHomeFeed(channelId: String): Flow<List<Video>> = flow {
        try {
            val videos = supabase.postgrest["videos"]
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, channels(name, logo_url), categories(name)")) {
                    filter { eq("channel_id", channelId) }
                }
                .decodeList<VideoDto>()
            emit(videos.map { it.toDomain() }.sortedByDescending { it.uploadDate })
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override fun getChannelVideos(channelId: String, isShort: Boolean, sortBy: String): Flow<List<Video>> = flow {
        try {
            val videos = supabase.postgrest["videos"]
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, channels(name, logo_url), categories(name)")) {
                    filter { 
                        eq("channel_id", channelId)
                        eq("is_short", isShort) 
                    }
                }
                .decodeList<VideoDto>()
            
            val mapped = videos.map { it.toDomain() }
            val sorted = when (sortBy) {
                "Oldest" -> mapped.sortedBy { it.uploadDate }
                "Popular" -> mapped.sortedByDescending { it.views }
                else -> mapped.sortedByDescending { it.uploadDate }
            }
            emit(sorted)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override suspend fun updateVideoMetadata(
        videoId: String,
        title: String,
        description: String,
        thumbnailUrl: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.updateVideoMetadata(videoId, title, description, thumbnailUrl)
            supabase.postgrest["videos"].update(
                {
                    set("title", title)
                    set("description", description)
                    set("thumbnail_url", thumbnailUrl)
                }
            ) {
                filter { eq("id", videoId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun uploadVideo(
        title: String,
        description: String,
        uri: String,
        isShort: Boolean,
        category: String,
        allowDownload: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // Handled in separate upload repository flow
        Result.success(Unit)
    }

    override suspend fun likeVideo(videoId: String) {
        // Handled via interaction table
    }

    override suspend fun saveVideo(videoId: String) {
        // Handled via interaction table
    }

    override suspend fun refreshFeed() = withContext(Dispatchers.IO) {
        try {
            val videos = supabase.postgrest["videos"]
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, channels(name, logo_url), categories(name)"))
                .decodeList<VideoDto>()
            
            val domainVideos = videos.map { it.toDomain() }
            
            // clear old to make sure we don't have stale data
            dao.clearVideos(true)
            dao.clearVideos(false)
            dao.insertVideos(domainVideos.map { it.toEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
