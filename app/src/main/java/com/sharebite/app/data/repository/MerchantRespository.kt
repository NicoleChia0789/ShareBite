package com.sharebite.app.data.repository

import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class NewFoodListing(
    @SerialName("merchant_id")
    val merchantId: String,
    @SerialName("food_name")
    val foodName: String,
    val category: String,
    val quantity: String,
    @SerialName("pickup_date")
    val pickupDate: String? = null,
    @SerialName("pickup_time")
    val pickupTime: String? = null,
    val notes: String? = null,
    val status: String = "available"
)

@Serializable
private data class ListingInsertResponse(
    @SerialName("id")
    val id: Long
)

data class ImpactStats(
    val totalWeight: Double,
    val totalMeals: Int,
    val co2Saved: Int,
    val totalDonations: Int,
    val totalListings: Int,
    val pendingRequests: Int,
    val confirmedReservations: Int
)

class MerchantRepository {

    // ──────────────────────────────────────────────
    // LISTINGS
    // ──────────────────────────────────────────────

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

    // ✅ Get all listings (including claimed/closed)
    suspend fun getAllMyListings(): List<FoodListing> {
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

    // ✅ Get active listings count
    suspend fun getActiveListingsCount(): Int {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["food_listings"]
                .select {
                    filter {
                        eq("merchant_id", merchantId)
                        eq("status", "available")
                    }
                }
                .decodeList<FoodListing>()
                .size
        } catch (e: Exception) {
            0
        }
    }

    // ✅ Create a new food listing
    suspend fun createListing(
        foodName: String,
        category: String,
        quantity: String,
        pickupDate: String? = null,
        pickupTime: String? = null,
        notes: String? = null
    ): Long {
        val merchantId = CurrentUser.getUserId()

        val newListing = NewFoodListing(
            merchantId = merchantId,
            foodName = foodName,
            category = category,
            quantity = quantity,
            pickupDate = pickupDate,
            pickupTime = pickupTime,
            notes = notes,
            status = "available"
        )

        val result = SupabaseClient.client.postgrest["food_listings"]
            .insert(newListing) {
                select(Columns.list("id"))
            }
            .decodeList<ListingInsertResponse>()
            .firstOrNull()

        return result?.id ?: 0L
    }

    // ✅ Update listing status
    suspend fun updateListingStatus(listingId: Long, status: String) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["food_listings"]
            .update(
                { set("status", status) }
            ) {
                filter {
                    eq("id", listingId)
                    eq("merchant_id", merchantId)
                }
            }
    }

    // ✅ Delete listing
    suspend fun deleteListing(listingId: Long): Boolean {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["food_listings"]
                .delete {
                    filter {
                        eq("id", listingId)
                        eq("merchant_id", merchantId)
                    }
                }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ✅ Get listing by ID
    suspend fun getListingById(listingId: Long): FoodListing? {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["food_listings"]
                .select {
                    filter {
                        eq("id", listingId)
                        eq("merchant_id", merchantId)
                    }
                    limit(1)
                }
                .decodeSingle<FoodListing>()
        } catch (e: Exception) {
            null
        }
    }

    // ──────────────────────────────────────────────
    // CHARITY REQUESTS
    // ──────────────────────────────────────────────

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

    // ✅ Get request by ID
    suspend fun getRequestById(requestId: Long): CharityRequest? {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["charity_requests"]
                .select {
                    filter {
                        eq("id", requestId)
                        eq("merchant_id", merchantId)
                    }
                    limit(1)
                }
                .decodeSingle<CharityRequest>()
        } catch (e: Exception) {
            null
        }
    }

    // ✅ Get pending requests count
    suspend fun getPendingRequestsCount(): Int {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["charity_requests"]
                .select {
                    filter {
                        eq("merchant_id", merchantId)
                        eq("status", "Pending")
                    }
                }
                .decodeList<CharityRequest>()
                .size
        } catch (e: Exception) {
            0
        }
    }

    // ✅ Update request status (approve/decline)
    suspend fun updateRequestStatus(requestId: Long, status: String) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["charity_requests"]
            .update(
                { set("status", status) }
            ) {
                filter {
                    eq("id", requestId)
                    eq("merchant_id", merchantId)
                }
            }
    }

    // ──────────────────────────────────────────────
    // RESERVATIONS
    // ──────────────────────────────────────────────

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

    // ✅ Get reservation by ID
    suspend fun getReservationById(reservationId: Long): Reservation? {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter {
                        eq("id", reservationId)
                        eq("merchant_id", merchantId)
                    }
                    limit(1)
                }
                .decodeSingle<Reservation>()
        } catch (e: Exception) {
            null
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

    // ✅ Update reservation status (confirm/pickup/complete)
    suspend fun updateReservationStatus(reservationId: Long, status: String) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["reservations"]
            .update(
                { set("status", status) }
            ) {
                filter {
                    eq("id", reservationId)
                    eq("merchant_id", merchantId)
                }
            }
    }

    // ──────────────────────────────────────────────
    // IMPACT STATS
    // ──────────────────────────────────────────────

    // ✅ Get impact stats
    suspend fun getImpactStats(): ImpactStats {
        val merchantId = CurrentUser.getUserId()

        // Get all listings
        val allListings = try {
            SupabaseClient.client.postgrest["food_listings"]
                .select {
                    filter { eq("merchant_id", merchantId) }
                }
                .decodeList<FoodListing>()
        } catch (e: Exception) {
            emptyList()
        }

        // Get all requests
        val allRequests = try {
            SupabaseClient.client.postgrest["charity_requests"]
                .select {
                    filter { eq("merchant_id", merchantId) }
                }
                .decodeList<CharityRequest>()
        } catch (e: Exception) {
            emptyList()
        }

        // Get all reservations
        val allReservations = try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter { eq("merchant_id", merchantId) }
                }
                .decodeList<Reservation>()
        } catch (e: Exception) {
            emptyList()
        }

        // Calculate impact
        val completedListings = allListings.filter { it.status == "completed" }
        val totalWeight = completedListings.sumOf { it.quantity.toDoubleOrNull() ?: 0.0 }
        val totalMeals = (totalWeight * 2).toInt()
        val co2Saved = (totalWeight * 0.8).toInt()

        return ImpactStats(
            totalWeight = totalWeight,
            totalMeals = totalMeals,
            co2Saved = co2Saved,
            totalDonations = completedListings.size,
            totalListings = allListings.size,
            pendingRequests = allRequests.count { it.status == "Pending" },
            confirmedReservations = allReservations.count { it.status == "Confirmed" }
        )
    }

    // Add this function to MerchantRepository.kt
// ✅ Get ALL general requests (charity posts with no specific merchant)
    suspend fun getAllGeneralRequests(): List<CharityRequest> {
        return try {
            // Fetch all requests first
            val allRequests = SupabaseClient.client.postgrest["charity_requests"]
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CharityRequest>()

            // Then filter in memory (not efficient for large datasets)
            allRequests.filter { it.merchantId == null }
        } catch (e: Exception) {
            emptyList()
        }
    }


}