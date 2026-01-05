package com.sangyoon.parkingpass.domain.model

enum class MemberRole {
    OWNER, ADMIN, MEMBER
}

enum class MemberStatus {
    PENDING, APPROVED, REJECTED
}

data class ParkingLotMember(
    val id: Long,
    val userId: String,
    val email: String,
    val name: String?,
    val role: MemberRole,
    val status: MemberStatus,
    val invitedBy: String?,
    val joinedAt: String?
)
