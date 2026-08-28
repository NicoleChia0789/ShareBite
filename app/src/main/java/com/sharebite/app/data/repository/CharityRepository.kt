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

    // 获取所有请求，可按状态过滤（如果 statusFilter 为 null，则获取全部）
    suspend fun getRequests(statusFilter: String? = null): List<CharityRequest> {
        return SupabaseClient.client.postgrest["charity_requests"]
            .select {
                if (statusFilter != null) {
                    filter { eq("status", statusFilter) }
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<CharityRequest>()
    }

    // 根据 ID 获取单条请求（用于详情页）
    suspend fun getRequestById(id: Long): CharityRequest {
        return SupabaseClient.client.postgrest["charity_requests"]
            .select {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeSingle<CharityRequest>()
    }

    // 创建新的食物请求
    suspend fun createRequest(
        foodName: String,
        category: String,
        quantity: Int,
        pickupDate: String,
        pickupTime: String,
        organisationName: String,
        peopleServed: Int,
        note: String?
    ) {
        val newRequest = CharityRequest(
            foodName = foodName,
            category = category,
            quantity = quantity,
            pickupDate = pickupDate,
            pickupTime = pickupTime,
            organisationName = organisationName,
            peopleServed = peopleServed,
            status = "Pending",
            note = note
        )
        SupabaseClient.client.postgrest["charity_requests"]
            .insert(newRequest)
    }

    // 针对某一笔已存在的食物提交申请（Apply For Food 专用，会关联 merchantId 和 listingId）
    suspend fun createRequestFromListing(
        merchantId: String?,
        listingId: String,
        foodName: String,
        category: String,
        quantity: Int,
        pickupDate: String,
        pickupTime: String,
        organisationName: String,
        peopleServed: Int,
        note: String?
    ) {
        val newRequest = CharityRequest(
            merchantId = null,  // ⚠️ 暂时不存，因为目前 food_listings 的 merchant_id 不是真的 uuid，等商家账号系统做完再串接
            listingId = listingId,
            foodName = foodName,
            category = category,
            quantity = quantity,
            pickupDate = pickupDate,
            pickupTime = pickupTime,
            organisationName = organisationName,
            peopleServed = peopleServed,
            status = "Pending",
            note = note
        )
        SupabaseClient.client.postgrest["charity_requests"].insert(newRequest)
    }


}