package com.sharebite.app.data.repository

import com.sharebite.app.data.model.CharityRequest
import com.sharebite.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class CharityRepository {

    // 目前还没做登入功能，先抓全部资料（之后加了登入/Auth 再改成只抓自己充善机构的资料）
    suspend fun getAllRequests(): List<CharityRequest> {
        return SupabaseClient.client.postgrest["charity_requests"]
            .select()
            .decodeList<CharityRequest>()
    }

    suspend fun getRecentConfirmed(limit: Long = 3): List<CharityRequest> {
        return SupabaseClient.client.postgrest["charity_requests"]
            .select {
                filter { eq("status", "Confirmed") }
                order("created_at", Order.DESCENDING)
                limit(limit)
            }
            .decodeList<CharityRequest>()
    }
}