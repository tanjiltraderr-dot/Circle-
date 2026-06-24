package com.example.domain.model

data class Channel(
    val id: String,
    val userId: String,
    val name: String,
    val handle: String,
    val description: String = "",
    val category: String = "",
    val profileImageUrl: String = "",
    val bannerImageUrl: String = "",
    val subscriberCount: Int = 0,
    val createdAt: String = ""
)
