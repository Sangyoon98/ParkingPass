package com.sangyoon.parkingpass.parking.model

import com.sangyoon.parkingpass.common.InstantSerializer
import com.sangyoon.parkingpass.common.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
enum class MemberRole {
    OWNER,
    ADMIN,
    MEMBER
}

@Serializable
enum class MemberStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Serializable
data class ParkingLotMember(
    val id: Long = 0,
    @SerialName("parking_lot_id")
    val parkingLotId: Long,
    @SerialName("user_id")
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val role: MemberRole = MemberRole.MEMBER,
    val status: MemberStatus = MemberStatus.PENDING,
    @SerialName("invited_by")
    @Serializable(with = UUIDSerializer::class)
    val invitedBy: UUID? = null,
    @SerialName("joined_at")
    @Serializable(with = InstantSerializer::class)
    val joinedAt: Instant? = null
)
