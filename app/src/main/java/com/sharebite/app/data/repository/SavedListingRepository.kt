package com.sharebite.app.data.repository

import com.sharebite.app.data.model.SavedListing
import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class SavedListingRepository {

    suspend fun isSaved(listingId: Long): Boolean {
        val result = SupabaseClient.client.postgrest["saved_listings"]
            .select {
                filter { eq("listing_id", listingId) }
            }
            .decodeList<SavedListing>()
        return result.isNotEmpty()
    }

    suspend fun saveListing(listingId: Long) {
        SupabaseClient.client.postgrest["saved_listings"]
            .insert(SavedListing(listingId = listingId))
    }

    suspend fun unsaveListing(listingId: Long) {
        SupabaseClient.client.postgrest["saved_listings"].delete {
            filter { eq("listing_id", listingId) }
        }
    }

    suspend fun getSavedListingIds(): List<Long> {
        return SupabaseClient.client.postgrest["saved_listings"]
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<SavedListing>()
            .map { it.listingId }
    }
}