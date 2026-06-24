package com.example.domain.model

data class Video(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val thumbnailUrl: String,
    val creatorId: String,
    val creatorName: String,
    val creatorProfilePic: String?,
    val isShort: Boolean,
    val likes: Int = 0,
    val views: Int = 0,
    val commentsCount: Int = 0,
    val category: String,
    val isDownloadable: Boolean = true,
    val uploadDate: Long = System.currentTimeMillis(),
    val localVideoPath: String? = null
)
