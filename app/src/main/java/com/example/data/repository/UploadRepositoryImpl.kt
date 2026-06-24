package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.util.GenesisMatrix
import com.example.data.remote.SupabaseEdgeApi
import com.example.data.remote.UploadUrlRequest
import com.example.domain.repository.UploadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class UploadRepositoryImpl(
    private val context: Context,
    private val api: SupabaseEdgeApi,
    private val okHttpClient: OkHttpClient
) : UploadRepository {

    override suspend fun uploadFile(uri: Uri, fileName: String, mimeType: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Get Upload URL
            val authHeader = "Bearer ${GenesisMatrix.publicNodeKey}"
            val request = UploadUrlRequest(fileName = fileName, fileType = mimeType)
            val response = api.getUploadUrl(authHeader, request)
            val uploadUrl = response.uploadUrl

            // Read file data
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw Exception("Failed to read file from URI")

            // Step 2: Upload to Cloudflare R2
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val putRequest = Request.Builder()
                .url(uploadUrl)
                .put(requestBody)
                .build()

            val putResponse: Response = okHttpClient.newCall(putRequest).execute()
            
            if (putResponse.isSuccessful) {
                // Determine the public URL if possible or just return success
                // Based on the requirements, we just upload it.
                Result.success("Upload successful")
            } else {
                Result.failure(Exception("Upload failed with code: ${putResponse.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
