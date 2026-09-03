package com.sharebite.app.data.repository

import com.sharebite.app.data.model.Reservation
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class ReservationRepository {

    // ✅ Get reservations for the current user
    suspend fun getMyReservations(): List<Reservation> {
        val userId = CurrentUser.getUserId()
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

    // ✅ Get my reservations by status
    suspend fun getMyReservationsByStatus(status: String): List<Reservation> {
        val userId = CurrentUser.getUserId()
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

    // ✅ Get counts for user dashboard
    suspend fun getMyReservationCounts(): Map<String, Int> {
        val userId = CurrentUser.getUserId()
        val allReservations = try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter { eq("user_id", userId) }
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

    // ✅ Get single reservation by ID (ensure it belongs to current user)
    suspend fun getMyReservationById(id: Long): Reservation? {
        val userId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["reservations"]
                .select {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                    limit(1)
                }
                .decodeSingle<Reservation>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createMyReservation(
        listingId: Long,
        merchantId: String,
        foodName: String,
        pickupDate: String,
        pickupTime: String,
        quantityNum: Int,      // 新增
        unit: String,          // 新增
        note: String? = null
    ): Long {
        val userId = CurrentUser.getUserId()
        println("📝 Creating reservation with:")
        println("  userId: $userId")
        println("  listingId: $listingId")
        println("  merchantId: $merchantId")
        println("  foodName: $foodName")
        println("  quantityNum: $quantityNum")   // ✅ 打印新字段
        println("  unit: $unit")                // ✅ 打印新字段
        println("  pickupDate: $pickupDate")
        println("  pickupTime: $pickupTime")
        println("  note: $note")

        val newReservation = Reservation(
            userId = userId,
            listingId = listingId,
            merchantId = merchantId,
            foodName = foodName,
            pickupDate = pickupDate,
            pickupTime = pickupTime,
            status = "Pending",
            note = note,
            quantityNum = quantityNum,   // ✅ 传入新字段
            unit = unit                  // ✅ 传入新字段
        )

        return try {
            val result = SupabaseClient.client.postgrest["reservations"]
                .insert(newReservation) { select() }
                .decodeList<Reservation>()
                .firstOrNull()
            result?.id ?: 0L
        } catch (e: Exception) {
            println("❌ Error creating reservation: ${e.message}")
            e.printStackTrace()
            0L
        }
    }

    // ✅ Update my pickup time
    suspend fun updateMyPickupTime(
        reservationId: Long,
        newDate: String,
        newTime: String
    ) {
        val userId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["reservations"]
            .update(
                {
                    set("pickup_date", newDate)
                    set("pickup_time", newTime)
                }
            ) {
                filter {
                    eq("id", reservationId)
                    eq("user_id", userId) // ✅ Ensure it belongs to current user
                }
            }
    }

    // ✅ Cancel my reservation
    suspend fun cancelMyReservation(reservationId: Long) {
        val userId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["reservations"]
            .update(
                { set("status", "Cancelled") }
            ) {
                filter {
                    eq("id", reservationId)
                    eq("user_id", userId) // ✅ Ensure it belongs to current user
                }
            }
    }

    // ✅ Delete my reservation
    suspend fun deleteMyReservation(reservationId: Long) {
        val userId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["reservations"]
            .delete {
                filter {
                    eq("id", reservationId)
                    eq("user_id", userId) // ✅ Ensure it belongs to current user
                }
            }
    }


}