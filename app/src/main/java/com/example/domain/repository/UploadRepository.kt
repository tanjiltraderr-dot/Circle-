package com.example.domain.repository

import android.net.Uri

interface UploadRepository {
    suspend fun uploadFile(uri: Uri, fileName: String, mimeType: String): Result<String>
}
