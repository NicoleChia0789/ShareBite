package com.sharebite.app.data.repository

import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class FoodListingRepository {

    // ✅ Get all available listings (for browsing - all users see same available food)
    suspend fun getAvailableListings(): List<FoodListing> {
        return SupabaseClient.client.postgrest["food_listings"]
            .select {
                filter { eq("status", "available") }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FoodListing>()
    }

    // ✅ Get merchant's own listings (for merchant dashboard)
    suspend fun getMyListings(): List<FoodListing> {
        val merchantId = CurrentUser.getUserId()
        return SupabaseClient.client.postgrest["food_listings"]
            .select {
                filter {
                    eq("merchant_id", merchantId)
                    eq("status", "available")
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FoodListing>()
    }

    // ✅ Get merchant's own listings including claimed/closed
    suspend fun getAllMyListings(): List<FoodListing> {
        val merchantId = CurrentUser.getUserId()
        return SupabaseClient.client.postgrest["food_listings"]
            .select {
                filter { eq("merchant_id", merchantId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FoodListing>()
    }

    suspend fun getListingById(id: Long): FoodListing {
        return SupabaseClient.client.postgrest["food_listings"]
            .select {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeSingle<FoodListing>()
    }

    suspend fun getAvailableCount(): Int {
        return getAvailableListings().size
    }

    // ✅ Add a new food listing (for merchant)
    suspend fun createListing(
        foodName: String,
        category: String,
        quantity: String,
        expiryDate: String? = null,
        pickupAddress: String? = null
    ) {
        val merchantId = CurrentUser.getUserId()
        val newListing = FoodListing(
            merchantId = merchantId,
            foodName = foodName,
            category = category,
            quantity = quantity,
            status = "available",

        )
        SupabaseClient.client.postgrest["food_listings"]
            .insert(newListing)
    }

    // ✅ Update listing status (claim/close)
    suspend fun updateListingStatus(listingId: Long, status: String) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["food_listings"]
            .update(
                { set("status", status) }
            ) {
                filter {
                    eq("id", listingId)
                    eq("merchant_id", merchantId) // ✅ Ensure it belongs to current merchant
                }
            }
    }

    // ✅ Delete listing (only if it belongs to current merchant)
    suspend fun deleteListing(listingId: Long) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["food_listings"]
            .delete {
                filter {
                    eq("id", listingId)
                    eq("merchant_id", merchantId) // ✅ Ensure it belongs to current merchant
                }
            }
    }
}