package com.sharebite.app.data.repository

import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class FoodListingRepository {

    // 抓全部还"available"的食物
    suspend fun getAvailableListings(): List<FoodListing> {
        return SupabaseClient.client.postgrest["food_listings"]
            .select {
                filter { eq("status", "available") }
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
}