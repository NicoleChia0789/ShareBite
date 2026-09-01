package com.sharebite.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodListing(
    val id: Long = 0,

    @SerialName("merchant_id")
    val merchantId: String? = null,

    @SerialName("merchant_name")
    val merchantName: String = "",

    @SerialName("food_name")
    val foodName: String = "",

    val category: String = "",
    val description: String? = null,
    val quantity: String = "",

    @SerialName("photo_url")
    val photoUrl: String? = null,

    @SerialName("pickup_date")
    val pickupDate: String = "",

    val deadline: String = "",
    val address: String = "",

    @SerialName("distance_km")
    val distanceKm: Double = 0.0,

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null,

    val notes: String? = null,
    val status: String = "available",

    @SerialName("portion_info")
    val portionInfo: String? = null,

    @SerialName("created_at")
    val createdAt: String = ""


) {

}