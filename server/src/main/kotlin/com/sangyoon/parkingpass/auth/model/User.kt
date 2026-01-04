package com.sangyoon.parkingpass.auth.model

import com.sangyoon.parkingpass.common.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val email: String,
    val name: String? = null,
    @SerialName("created_at")
    val createdAt: Instant
)
