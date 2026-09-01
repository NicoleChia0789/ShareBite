package com.sharebite.app.data.repository

import com.sharebite.app.data.model.UserAddress
import com.sharebite.app.data.remote.SupabaseClient
import com.sharebite.app.data.session.CurrentUser
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class AddressRepository {

    // ✅ 获取当前用户的所有地址
    suspend fun getMyAddresses(): List<UserAddress> {
        val userId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["user_addresses"]
                .select {
                    filter { eq("user_id", userId) }
                    order("is_default", Order.DESCENDING)
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<UserAddress>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ 获取默认地址
    suspend fun getDefaultAddress(): UserAddress? {
        val userId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["user_addresses"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_default", true)
                    }
                    limit(1)
                }
                .decodeList<UserAddress>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // ✅ 添加新地址（自动同步到 users.address）
    suspend fun addAddress(
        label: String,
        street: String,
        city: String,
        state: String,
        zip: String,
        country: String,
        phone: String,
        isDefault: Boolean = false
    ): Long {
        val userId = CurrentUser.getUserId()

        if (isDefault) {
            try {
                SupabaseClient.client.postgrest["user_addresses"]
                    .update({ set("is_default", false) }) {
                        filter { eq("user_id", userId) }
                    }
            } catch (e: Exception) {
                // 忽略
            }
        }

        val newAddress = UserAddress(
            userId = userId,
            label = label,
            street = street,
            city = city,
            state = state,
            zip = zip,
            country = country,
            phone = phone,
            isDefault = isDefault
        )

        val result = SupabaseClient.client.postgrest["user_addresses"]
            .insert(newAddress) { select() }
            .decodeList<UserAddress>()
            .firstOrNull()

        val newId = result?.id ?: 0L

        // ✅ 如果是默认地址，同步更新 users 表的 address 列
        if (isDefault && newId > 0) {
            syncToUserTable(street, city, state, zip, country)
        }

        return newId
    }

    // ✅ 更新地址（自动同步到 users.address）
    suspend fun updateAddress(
        addressId: Long,
        label: String,
        street: String,
        city: String,
        state: String,
        zip: String,
        country: String,
        phone: String,
        isDefault: Boolean
    ): Boolean {
        val userId = CurrentUser.getUserId()

        if (isDefault) {
            try {
                SupabaseClient.client.postgrest["user_addresses"]
                    .update({ set("is_default", false) }) {
                        filter {
                            eq("user_id", userId)
                            neq("id", addressId)
                        }
                    }
            } catch (e: Exception) {
                // 忽略
            }
        }

        val success = try {
            SupabaseClient.client.postgrest["user_addresses"]
                .update(
                    {
                        set("label", label)
                        set("street", street)
                        set("city", city)
                        set("state", state)
                        set("zip", zip)
                        set("country", country)
                        set("phone", phone)
                        set("is_default", isDefault)
                    }
                ) {
                    filter {
                        eq("id", addressId)
                        eq("user_id", userId)
                    }
                }
            true
        } catch (e: Exception) {
            false
        }

        // ✅ 如果是默认地址，同步更新 users 表的 address 列
        if (isDefault && success) {
            syncToUserTable(street, city, state, zip, country)
        }

        return success
    }

    // ✅ 删除地址
    suspend fun deleteAddress(addressId: Long): Boolean {
        val userId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["user_addresses"]
                .delete {
                    filter {
                        eq("id", addressId)
                        eq("user_id", userId)
                    }
                }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ✅ 设为默认地址（自动同步到 users.address）
    suspend fun setDefaultAddress(addressId: Long): Boolean {
        val userId = CurrentUser.getUserId()
        val address = getAddressById(addressId)

        return try {
            // 取消所有默认
            SupabaseClient.client.postgrest["user_addresses"]
                .update({ set("is_default", false) }) {
                    filter { eq("user_id", userId) }
                }

            // 设置这个为默认
            SupabaseClient.client.postgrest["user_addresses"]
                .update({ set("is_default", true) }) {
                    filter {
                        eq("id", addressId)
                        eq("user_id", userId)
                    }
                }

            // ✅ 同步更新 users 表的 address 列
            if (address != null) {
                syncToUserTable(
                    address.street,
                    address.city,
                    address.state,
                    address.zip,
                    address.country
                )
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    // ✅ 根据 ID 获取地址
    suspend fun getAddressById(addressId: Long): UserAddress? {
        val userId = CurrentUser.getUserId()
        return try {
            SupabaseClient.client.postgrest["user_addresses"]
                .select {
                    filter {
                        eq("id", addressId)
                        eq("user_id", userId)
                    }
                    limit(1)
                }
                .decodeList<UserAddress>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // ✅ 同步地址到 users 表
    private suspend fun syncToUserTable(
        street: String,
        city: String,
        state: String,
        zip: String,
        country: String
    ) {
        val userId = CurrentUser.getUserId()
        val fullAddress = "$street, $city, $state $zip, $country"

        try {
            println("🔄 Syncing address to users table: $fullAddress")

            SupabaseClient.client.postgrest["users"]
                .update({ set("address", fullAddress) }) {
                    filter { eq("id", userId) }
                }

            // ✅ 同时更新 CurrentUser 缓存
            CurrentUser.user?.address = fullAddress

            println("✅ Users address synced successfully")
        } catch (e: Exception) {
            println("⚠️ Failed to sync address: ${e.message}")
        }
    }
}