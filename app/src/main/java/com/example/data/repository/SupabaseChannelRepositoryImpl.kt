package com.example.data.repository

import android.net.Uri
import com.example.domain.model.Channel
import com.example.domain.repository.ChannelRepository
import com.example.domain.repository.UploadRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    val name: String,
    val category: String? = null,
    @SerialName("logo_url") val profileImageUrl: String? = null,
    @SerialName("cover_photo_url") val bannerImageUrl: String? = null,
    @SerialName("followers_count") val subscriberCount: Int? = 0,
    @SerialName("created_at") val createdAt: String? = null
) {
    fun toDomain(): Channel {
        return Channel(
            id = id ?: "",
            userId = userId,
            name = name,
            handle = "@${name.replace(" ", "").lowercase()}",
            description = "",
            category = category ?: "",
            profileImageUrl = profileImageUrl ?: "",
            bannerImageUrl = bannerImageUrl ?: "",
            subscriberCount = subscriberCount ?: 0,
            createdAt = createdAt ?: ""
        )
    }
}

class SupabaseChannelRepositoryImpl(
    private val supabase: SupabaseClient,
    private val uploadRepository: UploadRepository
) : ChannelRepository {

    override suspend fun getUserChannel(): Channel? = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext null
            val channels = supabase.postgrest["channels"]
                .select {
                    filter { eq("user_id", userId) }
                }.decodeList<ChannelDto>()
            channels.firstOrNull()?.toDomain()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getChannel(channelId: String): Channel? = withContext(Dispatchers.IO) {
        try {
            val channels = supabase.postgrest["channels"]
                .select {
                    filter { eq("id", channelId) }
                }.decodeList<ChannelDto>()
            channels.firstOrNull()?.toDomain()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createChannel(
        name: String,
        handle: String,
        category: String,
        profileImageUri: Uri?,
        bannerImageUri: Uri?
    ): Result<Channel> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext Result.failure(Exception("Not logged in"))
            
            var profileUrl = ""
            var bannerUrl = ""

            // Upload profile image
            if (profileImageUri != null) {
                val fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg"
                val uploadResult = uploadRepository.uploadFile(profileImageUri, fileName, "image/jpeg")
                if (uploadResult.isSuccess) {
                    profileUrl = uploadResult.getOrNull() ?: ""
                }
            }

            // Upload banner image
            if (bannerImageUri != null) {
                val fileName = "banner_${userId}_${System.currentTimeMillis()}.jpg"
                val uploadResult = uploadRepository.uploadFile(bannerImageUri, fileName, "image/jpeg")
                if (uploadResult.isSuccess) {
                    bannerUrl = uploadResult.getOrNull() ?: ""
                }
            }

            val channelDto = ChannelDto(
                userId = userId,
                name = name,
                category = category,
                profileImageUrl = profileUrl.ifEmpty { null },
                bannerImageUrl = bannerUrl.ifEmpty { null }
            )

            val createdChannels = supabase.postgrest["channels"]
                .insert(channelDto) {
                    select()
                }.decodeList<ChannelDto>()
                
            val createdChannel = createdChannels.firstOrNull() ?: throw Exception("Failed to create channel")
            Result.success(createdChannel.toDomain())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
