package com.sharebite.app.data.repository

import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest

class SavedListingRepository {

    // ✅ Get saved listing IDs for current user
    suspend fun getMySavedListingIds(): List<Long> {
        val userId = CurrentUser.getUserId()
        return try {
            val result = SupabaseClient.client.postgrest["saved_listings"]
                .select {
                    filter { eq("charity_id", userId) }
                }
                .decodeList<Map<String, Any>>()
            result.mapNotNull { it["listing_id"] as? Long }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ Save a listing for current user
    suspend fun saveListing(listingId: Long) {
        val userId = CurrentUser.getUserId()
        try {
            SupabaseClient.client.postgrest["saved_listings"]
                .insert(
                    mapOf(
                        "charity_id" to userId,
                        "listing_id" to listingId
                    )
                )
        } catch (e: Exception) {
            // Ignore duplicates
        }
    }

    // ✅ Unsave a listing for current user
    suspend fun unsaveListing(listingId: Long) {
        val userId = CurrentUser.getUserId()
        try {
            SupabaseClient.client.postgrest["saved_listings"]
                .delete {
                    filter {
                        eq("charity_id", userId)
                        eq("listing_id", listingId)
                    }
                }
        } catch (e: Exception) {
            // Ignore
        }
    }

    // ✅ Check if listing is saved by current user
    suspend fun isSaved(listingId: Long): Boolean {
        val userId = CurrentUser.getUserId()
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