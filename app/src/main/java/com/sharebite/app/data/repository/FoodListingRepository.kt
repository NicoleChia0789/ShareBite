package com.sharebite.app.data.repository

import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.isExpired
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
            .filterNot { it.isExpired() }
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
            .filterNot { it.isExpired() }
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
            .filterNot { it.isExpired() }
    }

    suspend fun getListingById(id: Long): FoodListing {
        val listing = SupabaseClient.client.postgrest["food_listings"]
            .select {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeSingle<FoodListing>()
        if (listing.isExpired()) {
            throw IllegalStateException("This food listing has passed its deadline.")
        }
        return listing
    }

    suspend fun getAvailableCount(): Int {
        return getAvailableListings().size
    }

    suspend fun createListing(
        foodName: String,
        category: String,
        totalQuantity: Int,          // ✅ 已改好
        unit: String? = null,        // ✅ 已改好
        pickupDate: String? = null,
        pickupTime: String? = null,  // ✅ 已改好
        deadline: String? = null,
        address: String? = null,
        description: String? = null
    ) {
        val merchantId = CurrentUser.getUserId()
        val newListing = FoodListing(
            merchantId = merchantId,
            foodName = foodName,
            category = category,
            totalQuantity = totalQuantity,   // ✅ 改为 totalQuantity
            unit = unit,                     // ✅ 传入 unit
            pickupDate = pickupDate ?: "",   // 如有默认值可处理
            pickupTime = pickupTime,         // ✅ 传入 pickupTime
            deadline = deadline ?: "",
            address = address ?: "",
            description = description,
            status = "available"
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
