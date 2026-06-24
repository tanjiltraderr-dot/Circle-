package com.example.domain.repository

import com.example.domain.model.Channel
import android.net.Uri

interface ChannelRepository {
    suspend fun getUserChannel(): Channel?
    suspend fun createChannel(
        name: String,
        handle: String,
        category: String,
        profileImageUri: Uri?,
        bannerImageUri: Uri?
    ): Result<Channel>
    suspend fun getChannel(channelId: String): Channel?
}
