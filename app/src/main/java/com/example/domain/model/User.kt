package com.example.domain.model

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePicture: String? = null,
    val followers: Int = 0,
    val following: Int = 0
)
