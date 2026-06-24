package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Video

@Entity(tableName = "downloaded_videos")
data class DownloadedVideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val url: String,
    val thumbnailUrl: String,
    val creatorId: String,
    val creatorName: String,
    val creatorProfilePic: String?,
    val isShort: Boolean,
    val likes: Int,
    val views: Int,
    val commentsCount: Int,
    val category: String,
    val isDownloadable: Boolean,
    val uploadDate: Long,
    val localVideoPath: String
)

fun DownloadedVideoEntity.toDomain() = Video(
    id = id,
    title = title,
    description = description,
    url = url,
    thumbnailUrl = thumbnailUrl,
    creatorId = creatorId,
    creatorName = creatorName,
    creatorProfilePic = creatorProfilePic,
    isShort = isShort,
    likes = likes,
    views = views,
    commentsCount = commentsCount,
    category = category,
    isDownloadable = isDownloadable,
    uploadDate = uploadDate,
    localVideoPath = localVideoPath
)

fun Video.toDownloadedEntity(localVideoPath: String) = DownloadedVideoEntity(
    id = id,
    title = title,
    description = description,
    url = url,
    thumbnailUrl = thumbnailUrl,
    creatorId = creatorId,
    creatorName = creatorName,
    creatorProfilePic = creatorProfilePic,
    isShort = isShort,
    likes = likes,
    views = views,
    commentsCount = commentsCount,
    category = category,
    isDownloadable = isDownloadable,
    uploadDate = uploadDate,
    localVideoPath = localVideoPath
)
