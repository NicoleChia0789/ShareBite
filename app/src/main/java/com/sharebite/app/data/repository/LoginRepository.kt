package com.sharebite.app.data.repository

import com.sharebite.app.data.model.AppUser
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.util.PasswordUtils
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class LoginRepository {

    suspend fun isEmailRegistered(email: String): Boolean {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank()) return false

        return SupabaseClient.client.postgrest["users"]
            .select {
                filter { eq("email", normalizedEmail) }
                limit(1)
            }
            .decodeList<AppUser>()
            .isNotEmpty()
    }

    // ---------- 注册 ----------
    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: String
    ): AppUser {
        if (!PasswordUtils.isStrongPassword(password)) {
            throw Exception(PasswordUtils.STRONG_PASSWORD_REQUIREMENTS)
        }

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
            val role: String
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
            return user
        } catch (e: Exception) {
            println("🔴 登录异常: ${e.message}")
            throw e
        }
    }

    // ---------- 已登录用户修改密码 ----------
    suspend fun updatePassword(
        userId: String,
        currentPassword: String,
        newPassword: String
    ): Boolean {
        if (!PasswordUtils.isStrongPassword(newPassword)) return false

        return try {
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

            val isCurrentPasswordCorrect = PasswordUtils.verifyPassword(currentPassword, user.passwordHash)
            if (!isCurrentPasswordCorrect) {
                println("❌ Current password is incorrect")
                return false
            }

            val newPasswordHash = PasswordUtils.hashPassword(newPassword)

            // ✅ 更新后验证是否成功
            val updateResult = SupabaseClient.client.postgrest["users"]
                .update(
                    { set("password_hash", newPasswordHash) }
                ) {
                    filter { eq("id", userId) }
                }

            // ✅ 验证更新是否真的生效
            val updatedUser = SupabaseClient.client.postgrest["users"]
                .select {
                    filter { eq("id", userId) }
                    limit(1)
                }
                .decodeList<AppUser>()
                .firstOrNull()

            if (updatedUser != null && updatedUser.passwordHash == newPasswordHash) {
                println("✅ Password updated successfully")
                true
            } else {
                println("❌ Password update verification failed")
                false
            }
        } catch (e: Exception) {
            println("❌ Failed to update password: ${e.message}")
            false
        }
    }

    // ---------- 通过邮箱重置密码（忘记密码用） ----------
    suspend fun updatePasswordByEmail(email: String, newPassword: String): Boolean {
        return try {
            val normalizedEmail = email.trim()
            if (normalizedEmail.isBlank() || !PasswordUtils.isStrongPassword(newPassword)) return false

            // 1️⃣ 先确认用户存在
            val user = SupabaseClient.client.postgrest["users"]
                .select {
                    filter { eq("email", normalizedEmail) }
                    limit(1)
                }
                .decodeList<AppUser>()
                .firstOrNull()

            if (user == null) {
                println("❌ updatePasswordByEmail: User not found with email: $normalizedEmail")
                return false
            }

            println("🔍 updatePasswordByEmail: 找到用户 ${user.id}")

            // 2️⃣ 加密新密码
            val hashed = PasswordUtils.hashPassword(newPassword)

            // 3️⃣ 更新密码（使用 id 更可靠）
            SupabaseClient.client.postgrest["users"]
                .update(
                    { set("password_hash", hashed) }
                ) {
                    filter { eq("id", user.id) }
                }

            // 4️⃣ 验证更新是否成功
            val updatedUser = SupabaseClient.client.postgrest["users"]
                .select {
                    filter { eq("id", user.id) }
                    limit(1)
                }
                .decodeList<AppUser>()
                .firstOrNull()

            if (updatedUser != null && updatedUser.passwordHash == hashed) {
                println("✅ updatePasswordByEmail: Password updated successfully")
                true
            } else {
                println("❌ updatePasswordByEmail: Password update verification failed")
                false
            }
        } catch (e: Exception) {
            println("❌ updatePasswordByEmail error: ${e.message}")
            false
        }
    }
}