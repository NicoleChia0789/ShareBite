package com.sharebite.app.data.repository

import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class MerchantRepository {

    // ✅ Get merchant's own listings from Supabase
    suspend fun getMyListings(): List<FoodListing> {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["food_listings"]
                .select {
                    filter { eq("merchant_id", merchantId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<FoodListing>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ Get requests for merchant's listings (pending approvals)
    suspend fun getMyPendingRequests(): List<CharityRequest> {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["charity_requests"]
                .select {
                    filter {
                        eq("merchant_id", merchantId)
                        eq("status", "Pending")
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CharityRequest>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ Get all requests for merchant's listings
    suspend fun getMyAllRequests(): List<CharityRequest> {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["charity_requests"]
                .select {
                    filter { eq("merchant_id", merchantId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CharityRequest>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ Get reservations for merchant's listings
    suspend fun getMyReservations(): List<Reservation> {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter { eq("merchant_id", merchantId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Reservation>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ Get reservation counts for merchant dashboard
    suspend fun getMyReservationCounts(): Map<String, Int> {
        val merchantId = CurrentUser.getUserId()
        val allReservations = try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter { eq("merchant_id", merchantId) }
                }
                .decodeList<Reservation>()
        } catch (e: Exception) {
            emptyList()
        }

        return mapOf(
            "pending" to allReservations.count { it.status == "Pending" },
            "confirmed" to allReservations.count { it.status == "Confirmed" },
            "completed" to allReservations.count { it.status == "Completed" },
            "cancelled" to allReservations.count { it.status == "Cancelled" }
        )
    }

    // ✅ Update request status (accept/reject)
    suspend fun updateRequestStatus(requestId: Long, status: String) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["charity_requests"]
            .update(
                { set("status", status) }
            ) {
                filter {
                    eq("id", requestId)
                    eq("merchant_id", merchantId) // ✅ Ensure it belongs to current merchant
                }
            }
    }

    // ✅ Update reservation status (confirm/pickup/complete)
    suspend fun updateReservationStatus(reservationId: Long, status: String) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["reservations"]
            .update(
                { set("status", status) }
            ) {
                filter {
                    eq("id", reservationId)
                    eq("merchant_id", merchantId) // ✅ Ensure it belongs to current merchant
                }
            }
    }
}