package com.sharebite.app.data.repository

import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SavedListingRepository {

    suspend fun getSavedListingIds(userId: String): List<Long> {
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

    suspend fun saveListing(userId: String, listingId: Long) {
        SupabaseClient.client.postgrest["saved_listings"]
            .insert(mapOf(
                "charity_id" to userId,
                "listing_id" to listingId
            ))
    }

    suspend fun unsaveListing(userId: String, listingId: Long) {
        SupabaseClient.client.postgrest["saved_listings"]
            .delete {
                filter {
                    eq("charity_id", userId)
                    eq("listing_id", listingId)
                }
            }
    }
}