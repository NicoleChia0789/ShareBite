package com.sharebite.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reservation(
    val id: Long = 0,

    @SerialName("user_id")
    val userId: String = "",

    @SerialName("listing_id")
    val listingId: Long = 0,

    @SerialName("merchant_id")
    val merchantId: String = "",

    @SerialName("food_name")
    val foodName: String = "",

    val quantity: String = "",

    @SerialName("pickup_date")
    val pickupDate: String = "",

    @SerialName("pickup_time")
    val pickupTime: String = "",

    // Pending / Confirmed / Cancelled / Completed
    // 大写开头，跟 charity 统一
    val status: String = "Pending",

    val note: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)