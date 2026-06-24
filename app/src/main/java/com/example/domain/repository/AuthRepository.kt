package com.example.domain.repository

import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun verifyPasswordResetOtp(email: String, otp: String): Result<Unit>
    suspend fun updatePassword(password: String): Result<Unit>
}
