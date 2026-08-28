package com.sharebite.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavedListing(
    val id: Long = 0,

    @SerialName("listing_id")
    val listingId: Long,

    @SerialName("charity_id")
    val charityId: String? = null,

    @SerialName("created_at")
    val createdAt: String = ""
)