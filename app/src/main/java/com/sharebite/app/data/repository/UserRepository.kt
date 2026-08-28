package com.sharebite.app.data.repository

import com.sharebite.app.data.model.User
import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class UserRepository {

    // ✅ 修改：捕获异常，返回 null
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