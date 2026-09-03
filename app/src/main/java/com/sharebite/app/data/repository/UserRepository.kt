package com.sharebite.app.data.repository

import android.content.Context
import android.net.Uri
import com.sharebite.app.data.model.User
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {

    // ✅ Get current user's profile
    suspend fun getMyProfile(): User? {
        val userId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["users"]
                .select {
                    filter { eq("id", userId) }
                    limit(1)
                }
                .decodeSingle<User>()
        } catch (e: Exception) {
            null
        }
    }

    // ✅ Get user by ID (for viewing other profiles if needed)
    suspend fun getUser(userId: String): User? {
        return try {
            SupabaseClient.client.postgrest["users"]
                .select {
                    filter { eq("id", userId) }
                    limit(1)
                }
                .decodeSingle<User>()
        } catch (e: Exception) {
            null
        }
    }

    // ✅ Update current user's profile
    suspend fun updateMyProfile(
        name: String,
        email: String,
        phone: String?,
        address: String?,
        avatarUrl: String? = null
    ) {
        val userId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["users"]
            .update(
                {
                    set("name", name)
                    set("email", email)
                    if (phone != null) set("phone", phone)
                    if (address != null) set("address", address)
                    if (avatarUrl != null) set("avatar_url", avatarUrl)
                }
            ) {
                filter { eq("id", userId) }
            }
    }

    // ✅ Update user by ID (admin only)
    suspend fun updateUser(
        userId: String,
        name: String,
        email: String,
        phone: String,
        address: String,
        avatarUrl: String? = null
    ) {
        SupabaseClient.client.postgrest["users"]
            .update(
                {
                    set("name", name)
                    set("email", email)
                    set("phone", phone)
                    set("address", address)
                    if (avatarUrl != null) set("avatar_url", avatarUrl)
                }
            ) {
                filter { eq("id", userId) }
            }
    }

    suspend fun uploadProfilePhoto(context: Context, photoUri: Uri, userId: String): String {
        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(photoUri)?.use { it.readBytes() }
        } ?: throw IllegalStateException("Unable to read the selected profile photo")

        val fileName = "profiles/${userId}_${System.currentTimeMillis()}.jpg"
        val bucket = SupabaseClient.client.storage.from("photos")
        bucket.upload(fileName, bytes) { upsert = false }
        return bucket.publicUrl(fileName).ifBlank {
            throw IllegalStateException("Supabase did not return a profile photo URL")
        }
    }
}
