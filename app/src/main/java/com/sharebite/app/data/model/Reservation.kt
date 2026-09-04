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

    @SerialName("quantity_num")
    val quantityNum: Int = 1,

    @SerialName("unit")
    val unit: String = "units",

    @SerialName("food_name")
    val foodName: String = "",

    @SerialName("pickup_date")
    val pickupDate: String = "",

    @SerialName("pickup_time")
    val pickupTime: String = "",

    val status: String = "Pending",

    val note: String? = null,

    @SerialName("cancellation_reason")
    val cancellationReason: String? = null,

    @SerialName("cancelled_at")
    val cancelledAt: String? = null,

    @SerialName("cancelled_by")
    val cancelledBy: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)
