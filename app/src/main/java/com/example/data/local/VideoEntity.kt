package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Video

@Entity(tableName = "videos")
data class VideoEntity(
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
    val uploadDate: Long
)

fun VideoEntity.toDomain() = Video(
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
    uploadDate = uploadDate
)

fun Video.toEntity() = VideoEntity(
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
    uploadDate = uploadDate
)
