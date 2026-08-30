package com.sharebite.app.data.repository

import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class ListingIdRow(
    @SerialName("listing_id") val listingId: Long
)

class SavedListingRepository {

    suspend fun getSavedListingIds(userId: String): List<Long> {
        return try {
            SupabaseClient.client.postgrest["saved_listings"]
                .select(Columns.list("listing_id")) {
                    filter { eq("charity_id", userId) }
                }
                .decodeList<ListingIdRow>()
                .map { it.listingId }
        } catch (e: Exception) {
            println("🔴 getSavedListingIds 错误: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveListing(userId: String, listingId: Long) {
        try {
            if (isSaved(userId, listingId)) {
                return
            }
            SupabaseClient.client.postgrest["saved_listings"]
                .insert(
                    mapOf(
                        "charity_id" to userId,
                        "listing_id" to listingId
                    )
                )
        } catch (e: Exception) {
            println("🔴 saveListing 错误: ${e.message}")
        }
    }

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
            println("🔴 unsaveListing 错误: ${e.message}")
        }
    }

    suspend fun isSaved(userId: String, listingId: Long): Boolean {
        return try {
            SupabaseClient.client.postgrest["saved_listings"]
                .select(Columns.list("listing_id")) {
                    filter {
                        eq("charity_id", userId)
                        eq("listing_id", listingId)
                    }
                    limit(1)
                }
                .decodeList<ListingIdRow>()
                .isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}