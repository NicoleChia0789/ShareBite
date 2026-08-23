package com.sharebite.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharityRequest(
    val id: Long = 0,

    @SerialName("charity_id")
    val charityId: String? = null,

    @SerialName("merchant_id")
    val merchantId: String? = null,

    @SerialName("listing_id")
    val listingId: String? = null,

    @SerialName("food_name")
    val foodName: String = "",

    val category: String = "",
    val quantity: Int = 0,

    @SerialName("pickup_date")
    val pickupDate: String = "",

    @SerialName("pickup_time")
    val pickupTime: String = "",

    @SerialName("organisation_name")
    val organisationName: String = "",

    @SerialName("people_served")
    val peopleServed: Int = 0,

    val status: String = "Pending",
    val note: String? = null,

    @SerialName("created_at")
    val createdAt: String = ""
)