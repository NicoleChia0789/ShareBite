package com.sharebite.app.data.repository

import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class MerchantRepository {

    // Fetch Merchant's Listings
    suspend fun getMerchantListings(merchantId: String): List<FoodListing> {
        return SupabaseClient.client.postgrest["food_listings"]
            .select {
                filter { eq("merchant_id", merchantId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }

    // Add a new Listing
    suspend fun addListing(listing: FoodListing) {
        SupabaseClient.client.postgrest["food_listings"].insert(listing)
    }

    // Fetch Charity Requests
    suspend fun getRequests(merchantId: String): List<CharityRequest> {
        return SupabaseClient.client.postgrest["charity_requests"]
            .select {
                filter { eq("merchant_id", merchantId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }

    // Fetch User Reservations
    suspend fun getReservations(merchantId: String): List<Reservation> {
        return try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter { eq("merchant_id", merchantId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}