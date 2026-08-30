package com.sharebite.app.data.repository

import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class RemainingQuantityRow(
    @SerialName("remaining_quantity") val remainingQuantity: Int
)

@Serializable
private data class InsertIdRow(
    @SerialName("id") val id: Long
)

class ReservationRepository {

    suspend fun createReservation(
        userId: String,
        listingId: Long,
        merchantId: String,
        foodName: String,
        quantity: String,
        pickupDate: String,
        pickupTime: String,
        note: String
    ) {
        val newReservation = Reservation(
            userId = userId,
            listingId = listingId,
            merchantId = merchantId,
            foodName = foodName,
            quantity = quantity,
            pickupDate = pickupDate,
            pickupTime = pickupTime,
            status = "Pending",
            note = note
        )
        SupabaseClient.client.postgrest["reservations"]
            .insert(newReservation)

        val current = SupabaseClient.client.postgrest["food_listings"]
            .select(Columns.list("remaining_quantity")) {
                filter { eq("id", listingId) }
                limit(1)
            }
            .decodeList<RemainingQuantityRow>()
            .firstOrNull()

        val newRemaining = (current?.remainingQuantity ?: 1) - 1
        SupabaseClient.client.postgrest["food_listings"]
            .update(
                {
                    set("remaining_quantity", newRemaining)
                }
            ) {
                filter { eq("id", listingId) }
            }
    }

    // ✅ 新增：创建预订并返回 ID
    suspend fun createReservationAndGetId(
        userId: String,
        listingId: Long,
        merchantId: String,
        foodName: String,
        quantity: String,
        pickupDate: String,
        pickupTime: String,
        note: String
    ): Long {
        val newReservation = Reservation(
            userId = userId,
            listingId = listingId,
            merchantId = merchantId,
            foodName = foodName,
            quantity = quantity,
            pickupDate = pickupDate,
            pickupTime = pickupTime,
            status = "Pending",
            note = note
        )

        // 插入并返回生成的 ID
        val result = SupabaseClient.client.postgrest["reservations"]
            .insert(newReservation) {
                select(Columns.list("id"))
            }
            .decodeList<InsertIdRow>()
            .firstOrNull()

        val newId = result?.id ?: 0L
        println("✅ 新创建的 Reservation ID: $newId")

        // 更新 food_listings 的剩余数量
        val current = SupabaseClient.client.postgrest["food_listings"]
            .select(Columns.list("remaining_quantity")) {
                filter { eq("id", listingId) }
                limit(1)
            }
            .decodeList<RemainingQuantityRow>()
            .firstOrNull()

        val newRemaining = (current?.remainingQuantity ?: 1) - 1
        SupabaseClient.client.postgrest["food_listings"]
            .update(
                {
                    set("remaining_quantity", newRemaining)
                }
            ) {
                filter { eq("id", listingId) }
            }

        return newId
    }

    suspend fun getUserReservations(userId: String): List<Reservation> {
        return try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Reservation>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserReservationsByStatus(
        userId: String,
        status: String
    ): List<Reservation> {
        return try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("status", status)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Reservation>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getReservationById(id: Long): Reservation? {
        return try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter { eq("id", id) }
                    limit(1)
                }
                .decodeSingle<Reservation>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updatePickupTime(
        reservationId: Long,
        newDate: String,
        newTime: String
    ) {
        SupabaseClient.client.postgrest["reservations"]
            .update(
                {
                    set("pickup_date", newDate)
                    set("pickup_time", newTime)
                }
            ) {
                filter { eq("id", reservationId) }
            }
    }

    suspend fun cancelReservation(reservationId: Long) {
        SupabaseClient.client.postgrest["reservations"]
            .update(
                { set("status", "Cancelled") }
            ) {
                filter { eq("id", reservationId) }
            }
    }

    suspend fun deleteReservation(reservationId: Long) {
        SupabaseClient.client.postgrest["reservations"]
            .delete {
                filter { eq("id", reservationId) }
            }
    }
}