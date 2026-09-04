package com.sharebite.app.data.repository

import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class CharityRepository {

    // ✅ Get requests for the logged-in charity only
    suspend fun getMyRequests(): List<CharityRequest> {
        val userId = CurrentUser.getUserId()
        return SupabaseClient.client.postgrest["charity_requests"]
            .select {
                filter { eq("charity_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<CharityRequest>()
    }

    // ✅ Get ALL requests (including those with merchant_id) for Merchant
    suspend fun getMyAllRequests(): List<CharityRequest> {
        val userId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["charity_requests"]
                .select {
                    filter { eq("charity_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CharityRequest>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ Get my requests by status
    suspend fun getMyRequestsByStatus(status: String): List<CharityRequest> {
        val userId = CurrentUser.getUserId()
        return SupabaseClient.client.postgrest["charity_requests"]
            .select {
                filter {
                    eq("charity_id", userId)
                    eq("status", status)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<CharityRequest>()
    }

    // ✅ Get counts for dashboard (pending, confirmed, completed, cancelled)
    suspend fun getMyRequestCounts(): Map<String, Int> {
        val userId = CurrentUser.getUserId()
        val allRequests = SupabaseClient.client.postgrest["charity_requests"]
            .select {
                filter { eq("charity_id", userId) }
            }
            .decodeList<CharityRequest>()

        return mapOf(
            "pending" to allRequests.count { it.status == "Pending" },
            "confirmed" to allRequests.count { it.status == "Confirmed" },
            "completed" to allRequests.count { it.status == "Completed" },
            "cancelled" to allRequests.count { it.status == "Cancelled" }
        )
    }

    // ✅ Get recent confirmed requests for dashboard
    suspend fun getMyRecentConfirmed(limit: Long = 3): List<CharityRequest> {
        val userId = CurrentUser.getUserId()
        return SupabaseClient.client.postgrest["charity_requests"]
            .select {
                filter {
                    eq("charity_id", userId)
                    eq("status", "Confirmed")
                }
                order("created_at", Order.DESCENDING)
                limit(limit)
            }
            .decodeList<CharityRequest>()
    }

    // ✅ Get single request by ID (ensure it belongs to this user)
    suspend fun getMyRequestById(id: Long): CharityRequest? {
        val userId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["charity_requests"]
                .select {
                    filter {
                        eq("id", id)
                        eq("charity_id", userId)
                    }
                    limit(1)
                }
                .decodeSingle<CharityRequest>()
        } catch (e: Exception) {
            null
        }
    }

    // ✅ Create new request (automatically uses current user ID)
    suspend fun createMyRequest(
        foodName: String,
        category: String,
        quantity: Int,
        pickupDate: String,
        pickupTime: String,
        organisationName: String,
        peopleServed: Int,
        note: String?
    ) {
        val userId = CurrentUser.getUserId()
        val newRequest = CharityRequest(
            charityId = userId,
            foodName = foodName,
            category = category,
            quantity = quantity,
            pickupDate = pickupDate,
            pickupTime = pickupTime,
            organisationName = organisationName,
            peopleServed = peopleServed,
            // Standalone Charity Bazaar requests wait for a merchant to offer help.
            status = "Pending",
            note = note
        )
        SupabaseClient.client.postgrest["charity_requests"]
            .insert(newRequest)
    }

    // ✅ Create request from listing (with current user ID)
    suspend fun createMyRequestFromListing(
        merchantId: String?,
        listingId: String,
        foodName: String,
        category: String,
        quantity: Int,
        pickupDate: String,
        pickupTime: String,
        organisationName: String,
        peopleServed: Int,
        note: String?
    ) {
        val userId = CurrentUser.getUserId()
        require(quantity > 0) { "Request quantity must be greater than zero" }
        val listing = getListing(listingId.toLongOrNull() ?: error("Invalid food listing"))
        require(listing.status == "available") { "This food listing is no longer available" }
        require(listing.remainingQuantity >= quantity) {
            "Only ${listing.remainingQuantity} ${listing.unit ?: "units"} remaining"
        }
        val newRequest = CharityRequest(
            charityId = userId,
            merchantId = merchantId,
            listingId = listingId,
            foodName = foodName,
            category = category,
            quantity = quantity,
            pickupDate = pickupDate,
            pickupTime = pickupTime,
            organisationName = organisationName,
            peopleServed = peopleServed,
            // Requests linked to a merchant listing are confirmed immediately.
            status = "Confirmed",
            note = note
        )
        val created = SupabaseClient.client.postgrest["charity_requests"]
            .insert(newRequest) { select() }
            .decodeList<CharityRequest>()
            .firstOrNull() ?: error("Failed to create request")
        try {
            updateListingInventory(listing, listing.remainingQuantity - quantity)
        } catch (error: Exception) {
            SupabaseClient.client.postgrest["charity_requests"].delete {
                filter { eq("id", created.id) }
            }
            throw error
        }
    }

    // ✅ Update my request (only if it belongs to current user)
    suspend fun updateMyRequest(
        requestId: Long,
        quantity: Int,
        pickupDate: String,
        pickupTime: String,
        peopleServed: Int,
        note: String?
    ) {
        val userId = CurrentUser.getUserId()
        require(quantity > 0) { "Request quantity must be greater than zero" }
        val existing = getMyRequestById(requestId) ?: error("Request not found")
        val listing = existing.listingId?.toLongOrNull()?.let { getListing(it) }
        val quantityDelta = quantity - existing.quantity
        if (listing != null && quantityDelta > 0) {
            require(listing.remainingQuantity >= quantityDelta) {
                "Only ${listing.remainingQuantity} ${listing.unit ?: "units"} remaining"
            }
            updateListingInventory(listing, listing.remainingQuantity - quantityDelta)
        } else if (listing != null && quantityDelta < 0) {
            updateListingInventory(listing, listing.remainingQuantity - quantityDelta)
        }
        try {
            SupabaseClient.client.postgrest["charity_requests"].update(
                {
                    set("quantity", quantity)
                    set("pickup_date", pickupDate)
                    set("pickup_time", pickupTime)
                    set("people_served", peopleServed)
                    set("note", note)
                }
            ) {
                filter {
                    eq("id", requestId)
                    eq("charity_id", userId)
                    eq("status", "Pending")
                }
            }
        } catch (error: Exception) {
            if (listing != null && quantityDelta != 0) {
                updateListingInventory(listing, listing.remainingQuantity + quantityDelta)
            }
            throw error
        }
    }

    // ✅ Withdraw my request
    suspend fun withdrawMyRequest(requestId: Long, reason: String) {
        val userId = CurrentUser.getUserId()
        val existing = getMyRequestById(requestId)
        SupabaseClient.client.postgrest["charity_requests"].update(
            {
                set("status", "Cancelled")
                set("cancellation_reason", reason)
                set("cancelled_at", java.time.Instant.now().toString())
                set("cancelled_by", userId)
            }
        ) {
            filter {
                eq("id", requestId)
                eq("charity_id", userId)
            }
        }
        if (existing != null && existing.status != "Cancelled" && existing.status != "Completed") {
            existing.listingId?.toLongOrNull()?.let { listingId ->
                getListing(listingId)?.let { listing ->
                    updateListingInventory(listing, listing.remainingQuantity + existing.quantity)
                }
            }
        }
    }

    private suspend fun getListing(listingId: Long): FoodListing {
        return SupabaseClient.client.postgrest["food_listings"]
            .select {
                filter { eq("id", listingId) }
                limit(1)
            }
            .decodeList<FoodListing>()
            .firstOrNull() ?: error("Food listing not found")
    }

    private suspend fun updateListingInventory(listing: FoodListing, newRemaining: Int) {
        val safeRemaining = newRemaining.coerceIn(0, listing.totalQuantity)
        SupabaseClient.client.postgrest["food_listings"].update({
            set("remaining_quantity", safeRemaining)
            set("status", if (safeRemaining == 0) "claimed" else "available")
        }) {
            filter { eq("id", listing.id) }
        }
    }
}
