package com.example.data.repository

import com.example.domain.model.User
import com.example.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    override fun getCurrentUser(): Flow<User?> {
        return supabase.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    val meta = user?.userMetadata
                    User(
                        id = user?.id ?: "",
                        firstName = meta?.get("first_name")?.let { if (it is kotlinx.serialization.json.JsonPrimitive) it.content else it.toString() } ?: "",
                        lastName = meta?.get("last_name")?.let { if (it is kotlinx.serialization.json.JsonPrimitive) it.content else it.toString() } ?: "",
                        email = user?.email ?: ""
                    )
                }
                else -> null
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser != null) {
                val meta = currentUser.userMetadata
                Result.success(User(
                    id = currentUser.id,
                    firstName = meta?.get("first_name")?.let { if (it is kotlinx.serialization.json.JsonPrimitive) it.content else it.toString() } ?: "",
                    lastName = meta?.get("last_name")?.let { if (it is kotlinx.serialization.json.JsonPrimitive) it.content else it.toString() } ?: "",
                    email = currentUser.email ?: ""
                ))
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            val result = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("first_name", firstName)
                    put("last_name", lastName)
                }
            }
            
            Result.success(User(
                id = result?.id ?: "",
                firstName = firstName,
                lastName = lastName,
                email = result?.email ?: ""
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return Result.failure(Exception("Not implemented yet"))
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(io.github.jan.supabase.auth.providers.builtin.OTP) {
                this.email = email
                createUser = false
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyPasswordResetOtp(email: String, otp: String): Result<Unit> {
        return try {
            supabase.auth.verifyEmailOtp(
                type = io.github.jan.supabase.auth.OtpType.Email.MAGIC_LINK,
                email = email,
                token = otp
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(password: String): Result<Unit> {
        return try {
            supabase.auth.updateUser {
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
