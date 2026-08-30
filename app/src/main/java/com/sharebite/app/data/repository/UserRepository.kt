package com.sharebite.app.data.repository

import com.sharebite.app.data.model.User
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest

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
        address: String
    ) {
        SupabaseClient.client.postgrest["users"]
            .update(
                {
                    set("name", name)
                    set("email", email)
                    set("phone", phone)
                    set("address", address)
                }
            ) {
                filter { eq("id", userId) }
            }
    }
}