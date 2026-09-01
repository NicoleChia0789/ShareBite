package com.sharebite.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUser(
    val id: String = "",
    var email: String = "",

    @SerialName("password_hash")
    val passwordHash: String = "",

    var name: String = "",
    var phone: String? = null,
    var address: String? = null,
    val role: String = "charity",

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String = ""
)