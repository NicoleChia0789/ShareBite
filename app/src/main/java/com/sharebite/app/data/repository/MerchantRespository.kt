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
import java.util.Objects.isNull

@Serializable
private data class NewFoodListing(
    @SerialName("merchant_id") val merchantId: String,
    @SerialName("merchant_name") val merchantName: String? = null,
    @SerialName("food_name") val foodName: String,
    val category: String,
    val description: String? = null,
    val quantity: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("pickup_date") val pickupDate: String? = null,
    // ✅ 移除 pickupTime
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
                    filter { eq("merchant_id", merchantId) }
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

    // ✅ 移除 pickupTime 参数
    suspend fun createListing(
        foodName: String,
        category: String,
        quantity: String,
        photoUri: String?,
        pickupDate: String?,
        notes: String?,
        description: String?,
        address: String?,
        distanceKm: Double,
        dietaryType: String?,
        totalQuantity: Int,
        remainingQuantity: Int
    ): Long {
        val merchantId = CurrentUser.getUserId()
        val newListing = NewFoodListing(
            merchantId = merchantId,
            merchantName = CurrentUser.getUserName(),
            foodName = foodName,
            category = category,
            description = description,
            quantity = quantity,
            photoUrl = photoUri,
            pickupDate = pickupDate,
            // ✅ 不再传入 pickupTime
            deadline = pickupDate, // 用 pickupDate 作为 deadline（或设为 null）
            address = address,
            distanceKm = distanceKm,
            latitude = null,
            longitude = null,
            notes = notes,
            status = "available",
            portionInfo = null,
            totalQuantity = totalQuantity,
            remainingQuantity = remainingQuantity,
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
        notes: String?,
        address: String?
    ) {
        val merchantId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["food_listings"]
            .update({
                set("food_name", foodName)
                set("category", category)
                set("quantity", quantity)
                set("photo_url", photoUrl)
                set("pickup_date", pickupDate)
                // ✅ 不再设置 pickup_time
                set("deadline", pickupDate) // 用 pickupDate 作为 deadline
                set("notes", notes)
                set("address", address)
            }) {
                filter {
                    eq("id", listingId)
                    eq("merchant_id", merchantId)
                }
            }
    }
}