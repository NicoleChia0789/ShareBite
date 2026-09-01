package com.sharebite.app.data.repository

import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 收藏（Saved Listings）数据仓库
 * 统一供 User 和 Charity 模块使用
 *
 * 表结构：saved_listings
 * - id          : bigint (主键)
 * - created_at  : timestamp
 * - listing_id  : bigint (关联 food_listings.id)
 * - charity_id  : uuid   (存储用户 UUID，无论是 user 还是 charity)
 *
 * 注意：字段名虽然是 "charity_id"，但实际存储的是任何用户的 UUID。
 */
class SavedListingRepository {

    /**
     * 获取用户收藏的所有 food listing ID 列表
     * @param userId 用户 UUID（实际存储于 charity_id 字段）
     * @return List<Long> 收藏的 listing ID，若出错返回空列表
     */
    suspend fun getSavedListingIds(userId: String): List<Long> {
        return try {
            val result = SupabaseClient.client.postgrest["saved_listings"]
                .select {
                    filter { eq("charity_id", userId) }
                }
                .decodeList<Map<String, Any>>()
            result.mapNotNull { it["listing_id"] as? Long }
        } catch (e: Exception) {
            // 若 userId 无效或网络问题，返回空列表，不崩溃
            emptyList()
        }
    }

    /**
     * 收藏一个食物 listing
     * @param userId 用户 UUID
     * @param listingId 要收藏的 listing ID
     */
    suspend fun saveListing(userId: String, listingId: Long) {
        try {
            SupabaseClient.client.postgrest["saved_listings"]
                .insert(
                    mapOf(
                        "charity_id" to userId,
                        "listing_id" to listingId
                    )
                )
        } catch (e: Exception) {
            // 忽略错误（例如重复收藏），不抛出异常
        }
    }

    /**
     * 取消收藏一个食物 listing
     * @param userId 用户 UUID
     * @param listingId 要取消收藏的 listing ID
     */
    suspend fun unsaveListing(userId: String, listingId: Long) {
        try {
            SupabaseClient.client.postgrest["saved_listings"]
                .delete {
                    filter {
                        eq("charity_id", userId)
                        eq("listing_id", listingId)
                    }
                }
        } catch (e: Exception) {
            // 忽略错误
        }
    }

    /**
     * 检查某个 listing 是否已被当前用户收藏
     * @param userId 用户 UUID
     * @param listingId 要检查的 listing ID
     * @return true 如果已收藏，false 否则
     */
    suspend fun isSaved(userId: String, listingId: Long): Boolean {
        return try {
            val result = SupabaseClient.client.postgrest["saved_listings"]
                .select {
                    filter {
                        eq("charity_id", userId)
                        eq("listing_id", listingId)
                    }
                    limit(1)
                }
                .decodeList<Map<String, Any>>()
            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}