package com.sharebite.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUser(
    val id: String = "",
    val email: String = "",

    @SerialName("password_hash")
    val passwordHash: String = "",

    val name: String = "",
    val phone: String? = null,
    val address: String? = null,
    val role: String = "charity",

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String = ""
)