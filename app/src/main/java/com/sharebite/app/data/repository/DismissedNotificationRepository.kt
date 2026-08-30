package com.sharebite.app.data.repository

import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DismissedNotification(
    val id: Long = 0,
    @SerialName("request_id")
    val requestId: Long,
    @SerialName("user_id")
    val userId: String // ✅ Add user ID
)

class DismissedNotificationRepository {

    // ✅ Get dismissed IDs for current user
    suspend fun getMyDismissedIds(): Set<Long> {
        val userId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["dismissed_notifications"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<DismissedNotification>()
                .map { it.requestId }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    // ✅ Dismiss notification for current user
    suspend fun dismiss(requestId: Long) {
        val userId = CurrentUser.getUserId()
        SupabaseClient.client.postgrest["dismissed_notifications"]
            .insert(
                DismissedNotification(
                    requestId = requestId,
                    userId = userId
                )
            )
    }
}