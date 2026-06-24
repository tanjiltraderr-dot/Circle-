package com.example.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadUrlRequest(
    val fileName: String,
    val fileType: String
)

@JsonClass(generateAdapter = true)
data class UploadUrlResponse(
    val uploadUrl: String
)

interface SupabaseEdgeApi {
    @POST("functions/v1/get-upload-url")
    suspend fun getUploadUrl(
        @Header("Authorization") authHeader: String,
        @Body request: UploadUrlRequest
    ): UploadUrlResponse
}
