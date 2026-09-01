package com.sharebite.app.data.repository

import com.sharebite.app.data.model.AppUser
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.util.PasswordUtils
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class LoginRepository {

    // ---------- 注册（新增 role 参数）----------
    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: String  // ← 新增
    ): AppUser {
        // 检查 email 是否已注册
        val existing = SupabaseClient.client.postgrest["users"]
            .select {
                filter { eq("email", email) }
            }
            .decodeList<AppUser>()

        if (existing.isNotEmpty()) {
            throw Exception("This email is already registered.")
        }

        val hashed = PasswordUtils.hashPassword(password)

        @Serializable
        data class NewUserPayload(
            val email: String,
            @SerialName("password_hash") val passwordHash: String,
            val name: String,
            val role: String  // ← 新增
        )

        val payload = NewUserPayload(email = email, passwordHash = hashed, name = name, role = role)
        return SupabaseClient.client.postgrest["users"].insert(payload) {
            select()
        }.decodeSingle<AppUser>()
    }

    // ---------- 登入 ----------
    suspend fun login(email: String, password: String): AppUser {
        println("🔍 1. 开始登入: $email")
        try {
            val results = SupabaseClient.client.postgrest["users"]
                .select {
                    filter { eq("email", email) }
                }
                .decodeList<AppUser>()
            println("🔍 2. 查询结果: ${results.size} 个用户")

            val user = results.firstOrNull()
                ?: throw Exception("No account found with this email.")

            val isCorrect = PasswordUtils.verifyPassword(password, user.passwordHash)
            if (!isCorrect) {
                throw Exception("Incorrect password.")
            }

            println("🔍 3. 密码验证通过！")
            return user  // ← 这里要返回 user，而不是 TODO()
        } catch (e: Exception) {
            println("🔴 登录异常: ${e.message}")
            throw e
        }

    }

    suspend fun updatePassword(
        userId: String,
        currentPassword: String,
        newPassword: String
    ): Boolean {
        return try {
            // 1. 先验证当前密码是否正确
            val user = SupabaseClient.client.postgrest["users"]
                .select {
                    filter { eq("id", userId) }
                    limit(1)
                }
                .decodeList<AppUser>()
                .firstOrNull()

            if (user == null) {
                println("❌ User not found")
                return false
            }

            // 2. 验证当前密码
            val isCurrentPasswordCorrect = PasswordUtils.verifyPassword(currentPassword, user.passwordHash)
            if (!isCurrentPasswordCorrect) {
                println("❌ Current password is incorrect")
                return false
            }

            // 3. 加密新密码
            val newPasswordHash = PasswordUtils.hashPassword(newPassword)

            // 4. 更新数据库中的密码
            SupabaseClient.client.postgrest["users"]
                .update(
                    { set("password_hash", newPasswordHash) }
                ) {
                    filter { eq("id", userId) }
                }

            println("✅ Password updated successfully")
            true
        } catch (e: Exception) {
            println("❌ Failed to update password: ${e.message}")
            false
        }
    }
}