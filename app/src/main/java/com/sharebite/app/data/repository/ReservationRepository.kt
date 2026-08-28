package com.sharebite.app.data.repository

import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

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
    }

    // ✅ 修改：捕获异常，返回空列表
    suspend fun getUserReservations(userId: String): List<Reservation> {
        return try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Reservation>()
        } catch (e: Exception) {
            // 如果 userId 无效或网络错误，返回空列表
            emptyList()
        }
    }

    // ✅ 修改：捕获异常，返回空列表
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

    // ✅ 修改：捕获异常，返回 null
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