package com.sharebite.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodListing(
    // ===== 基础信息 =====
    val id: Long = 0,

    @SerialName("merchant_id")
    val merchantId: String? = null,

    @SerialName("merchant_name")
    val merchantName: String = "",

    @SerialName("food_name")
    val foodName: String = "",

    val category: String = "",
    val description: String? = null,          // ⭐ 可空（允许无描述）

    val quantity: String = "",

    @SerialName("photo_url")
    val photoUrl: String? = null,

    // ===== 取餐信息 =====
    @SerialName("pickup_date")
    val pickupDate: String = "",

    val deadline: String = "",

    // ===== 地址 & 位置 =====
    val address: String = "",

    @SerialName("distance_km")
    var distanceKm: Double = 0.0,             // ⭐ var（支持动态更新距离）

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null,

    // ===== 备注 & 状态 =====
    val notes: String? = null,
    val status: String = "available",

    @SerialName("portion_info")
    val portionInfo: String? = null,

    // ===== 膳食偏好 =====
    @SerialName("dietary_type")               // ⭐ 新增（筛选功能已使用）
    val dietaryType: String? = null,

    // ===== 库存管理 =====
    @SerialName("total_quantity")             // ⭐ 新增（总数量）
    val totalQuantity: Int = 0,

    @SerialName("remaining_quantity")         // ⭐ 新增（剩余数量）
    val remainingQuantity: Int = 0,

    // ===== 时间戳 =====
    @SerialName("created_at")
    val createdAt: String = ""
)