package com.sharebite.app.data.repository

import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.model.FoodListing
import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.model.UserSummary
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Objects.isNull

@Serializable
private data class NewFoodListing(
    @SerialName("merchant_id") val merchantId: String,
    @SerialName("merchant_name") val merchantName: String? = null,
    @SerialName("food_name") val foodName: String,
    val category: String,
    val description: String? = null,
    // ✅ 已删除 val quantity: String
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("pickup_date") val pickupDate: String? = null,
    @SerialName("pickup_time") val pickupTime: String? = null,
    @SerialName("deadline") val deadline: String? = null,
    val address: String? = null,
    @SerialName("distance_km") val distanceKm: Double = 0.0,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    val notes: String? = null,
    val status: String = "available",
    @SerialName("portion_info") val portionInfo: String? = null,
    @SerialName("total_quantity") val totalQuantity: Int = 10,
    @SerialName("remaining_quantity") val remainingQuantity: Int = 10,
    @SerialName("dietary_type") val dietaryType: String? = null
)
@Serializable
private data class ListingInsertResponse(
    @SerialName("id") val id: Long
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

    suspend fun getMyListings(): List<FoodListing> {
        val merchantId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["food_listings"]
                .select {
                    filter {
                        eq("merchant_id", merchantId)
                        eq("status", "available")  // ⚠️ 只查 available 状态
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<FoodListing>()
        } catch (e: Exception) {
            emptyList()
        }
    }

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

    suspend fun getReservationUsers(): List<UserSummary> {
        return try {
            SupabaseClient.client.postgrest["users"]
                .select(columns = Columns.list("id", "name", "avatar_url")) {
                    filter { eq("role", "user") }
                }
                .decodeList<UserSummary>()
        } catch (e: Exception) {
            println("❌ [Repository] Failed to load reservation users: ${e.message}")
            emptyList()
        }
    }

    suspend fun getImpactStats(): ImpactStats {
        return ImpactStats(0.0, 0, 0, 0, 0, 0, 0)
    }

    suspend fun getAllGeneralRequests(): List<CharityRequest> {
        return try {
            println("🔍🔍🔍 [Repository] 开始查询 charity_requests 表...")

            // 1️⃣ 先查询所有数据（不加任何过滤）
            val all = SupabaseClient.client.postgrest["charity_requests"]
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CharityRequest>()

            println("🔍 [Repository] 查询到 ${all.size} 条记录")
            all.forEach {
                println("   id=${it.id}, merchantId='${it.merchantId}', org=${it.organisationName}")
            }

            // 2️⃣ 过滤出 merchant_id 为 null 或空字符串的记录
            val filtered = all.filter {
                it.merchantId == null || it.merchantId.isBlank()
            }

            println("✅ [Repository] 过滤后 ${filtered.size} 条记录")
            filtered.forEach {
                println("   ✅ ${it.organisationName}: ${it.foodName}")
            }

            filtered
        } catch (e: Exception) {
            println("❌ [Repository] 查询失败: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateRequestStatus(requestId: Long, status: String) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["charity_requests"]
            .update({ set("status", status) }) {
                filter {
                    eq("id", requestId)
                    eq("merchant_id", merchantId)
                }
            }
    }

    suspend fun updateReservationStatus(reservationId: Long, status: String) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["reservations"]
            .update({ set("status", status) }) {
                filter {
                    eq("id", reservationId)
                    eq("merchant_id", merchantId)
                }
            }
    }

    /**
     * Reserves stock for a pending reservation before confirming it.
     * All values use the existing bigint/integer columns from Supabase; no schema
     * change or new column is required.
     */
    suspend fun approveReservation(reservation: Reservation) {
        require(reservation.quantityNum > 0) { "Reservation quantity must be greater than zero" }
        val listing = getOwnedListing(reservation.listingId)
        require(listing.remainingQuantity >= reservation.quantityNum) {
            "Only ${listing.remainingQuantity} ${listing.unit ?: "units"} remaining"
        }
        updateListingInventory(
            listing = listing,
            newRemaining = listing.remainingQuantity - reservation.quantityNum
        )
        try {
            updateReservationStatus(reservation.id, "Confirmed")
        } catch (error: Exception) {
            updateListingInventory(listing, listing.remainingQuantity)
            throw error
        }
    }

    /** Reserves stock for a charity request that is linked to a merchant listing. */
    suspend fun approveLinkedRequest(request: CharityRequest) {
        val listingId = request.listingId?.toLongOrNull()
            ?: error("This charity request is not linked to a donation")
        require(request.quantity > 0) { "Request quantity must be greater than zero" }
        val listing = getOwnedListing(listingId)
        require(listing.remainingQuantity >= request.quantity) {
            "Only ${listing.remainingQuantity} ${listing.unit ?: "units"} remaining"
        }
        updateListingInventory(listing, listing.remainingQuantity - request.quantity)
        try {
            updateRequestStatus(request.id, "Confirmed")
        } catch (error: Exception) {
            updateListingInventory(listing, listing.remainingQuantity)
            throw error
        }
    }

    private suspend fun getOwnedListing(listingId: Long): FoodListing {
        val merchantId = CurrentUser.getUserId()
        return SupabaseClient.client.postgrest["food_listings"]
            .select {
                filter {
                    eq("id", listingId)
                    eq("merchant_id", merchantId)
                }
            }
            .decodeList<FoodListing>()
            .firstOrNull()
            ?: error("Donation listing not found")
    }

    private suspend fun updateListingInventory(listing: FoodListing, newRemaining: Int) {
        val safeRemaining = newRemaining.coerceIn(0, listing.totalQuantity)
        SupabaseClient.client.postgrest["food_listings"]
            .update({
                set("remaining_quantity", safeRemaining)
                set("status", if (safeRemaining == 0) "claimed" else "available")
            }) {
                filter {
                    eq("id", listing.id)
                    eq("merchant_id", CurrentUser.getUserId())
                }
            }
    }

    suspend fun createListing(
        foodName: String,
        category: String,
        // ✅ 已删除 quantity: String
        photoUrl: String?,
        pickupDate: String?,
        pickupTime: String?,
        notes: String?,
        description: String?,
        address: String?,
        distanceKm: Double,
        dietaryType: String?,
        totalQuantity: Int,
        remainingQuantity: Int  // ✅ 保留
    ): Long {
        println("🔍 [Repository] Received photoUrl: '$photoUrl'")
        val merchantId = CurrentUser.getUserId()
        val newListing = NewFoodListing(
            merchantId = merchantId,
            merchantName = CurrentUser.getUserName(),
            foodName = foodName,
            category = category,
            description = description,
            // ✅ 不再传入 quantity
            photoUrl = photoUrl,
            pickupDate = pickupDate,
            pickupTime = pickupTime,
            deadline = if (pickupDate != null && pickupTime != null) "$pickupDate $pickupTime" else null,
            address = address,
            distanceKm = distanceKm,
            latitude = null,
            longitude = null,
            notes = notes,
            status = "available",
            portionInfo = null,
            totalQuantity = totalQuantity,
            remainingQuantity = remainingQuantity,  // ✅ 使用传入的 remainingQuantity
            dietaryType = dietaryType
        )
        val result = SupabaseClient.client.postgrest["food_listings"]
            .insert(newListing) {
                select(Columns.list("id"))
            }
            .decodeList<ListingInsertResponse>()
            .firstOrNull()
        return result?.id ?: 0L
    }

    suspend fun deleteListing(listingId: Long) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["food_listings"]
            .delete {
                filter {
                    eq("id", listingId)
                    eq("merchant_id", merchantId)
                }
            }
    }

    // ✅ 移除 pickupTime 参数
    suspend fun updateListing(
        listingId: Long,
        foodName: String,
        category: String,
        quantity: String,
        photoUrl: String?,
        pickupDate: String?,
        pickupTime: String?,
        notes: String?,
        address: String?
    ) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["food_listings"]
            .update({
                set("food_name", foodName)
                set("category", category)
//                set("quantity", quantity)
                set("total_quantity", quantity.toIntOrNull() ?: 0)      // ✅ Use total_quantity
                set("remaining_quantity", quantity.toIntOrNull() ?: 0)  // ✅ Use remaining_quantity
                set("photo_url", photoUrl)
                set("pickup_date", pickupDate)
                set("pickup_time", pickupTime)
                set("deadline", if (pickupDate != null && pickupTime != null) "$pickupDate $pickupTime" else null)
                set("notes", notes)
                set("address", address)
            }) {
                filter {
                    eq("id", listingId)
                    eq("merchant_id", merchantId)
                }
            }
    }

    suspend fun claimRequest(requestId: Long, merchantId: String) {
        SupabaseClient.client.postgrest["charity_requests"]
            .update({
                set("merchant_id", merchantId)
                set("status", "Confirmed")  // 或 "Pending"，按业务需求
            }) {
                filter { eq("id", requestId) }
            }
    }

    suspend fun updateListingStatus(listingId: Long, status: String) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["food_listings"]
            .update({ set("status", status) }) {
                filter {
                    eq("id", listingId)
                    eq("merchant_id", merchantId)
                }
            }
    }
}
