package com.example.domain.model

data class Comment(
    val id: String,
    val videoId: String,
    val userId: String,
    val userName: String,
    val userProfilePic: String?,
    val content: String,
    val likes: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val replies: List<Comment> = emptyList()
)
