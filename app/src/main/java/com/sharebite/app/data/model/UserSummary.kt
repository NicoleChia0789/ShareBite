package com.sharebite.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSummary(
    val id: String,
    val name: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null
)