package com.sharebite.app.data.repository

import com.sharebite.app.data.model.CharityRequest
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
            status = "Pending",
            note = note
        )
        SupabaseClient.client.postgrest["charity_requests"].insert(newRequest)
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
    }

    // ✅ Withdraw my request
    suspend fun withdrawMyRequest(requestId: Long) {
        val userId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["charity_requests"].update(
            { set("status", "Cancelled") }
        ) {
            filter {
                eq("id", requestId)
                eq("charity_id", userId)
            }
        }
    }
}