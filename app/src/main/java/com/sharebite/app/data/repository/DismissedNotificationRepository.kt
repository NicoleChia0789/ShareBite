package com.sharebite.app.data.repository

import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DismissedNotification(
    val id: Long = 0,
    @SerialName("request_id")
    val requestId: Long
)

class DismissedNotificationRepository {

    suspend fun getDismissedIds(): Set<Long> {
        return SupabaseClient.client.postgrest["dismissed_notifications"]
            .select()
            .decodeList<DismissedNotification>()
            .map { it.requestId }
            .toSet()
    }

    suspend fun dismiss(requestId: Long) {
        SupabaseClient.client.postgrest["dismissed_notifications"]
            .insert(DismissedNotification(requestId = requestId))
    }
}