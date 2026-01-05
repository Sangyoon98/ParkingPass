package com.sangyoon.parkingpass.parking.model

import com.sangyoon.parkingpass.common.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ParkingLot(
    val id: Long = 0,
    val name: String,
    val location: String,
    @SerialName("owner_id")
    @Serializable(with = UUIDSerializer::class)
    val ownerId: UUID? = null,
    @SerialName("is_public")
    val isPublic: Boolean = true,
    @SerialName("join_code")
    val joinCode: String? = null
)
