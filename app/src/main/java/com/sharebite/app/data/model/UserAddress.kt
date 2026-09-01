package com.sharebite.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAddress(
    val id: Long = 0,
    @SerialName("user_id")
    val userId: String,
    val label: String,
    val street: String,
    val city: String,
    val state: String,
    val zip: String,
    val country: String,
    val phone: String,
    @SerialName("is_default")
    val isDefault: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)