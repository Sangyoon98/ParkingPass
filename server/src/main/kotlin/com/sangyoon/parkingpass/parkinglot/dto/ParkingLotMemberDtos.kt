package com.sangyoon.parkingpass.parkinglot.dto

import com.sangyoon.parkingpass.parking.model.MemberRole
import com.sangyoon.parkingpass.parking.model.MemberStatus
import kotlinx.serialization.Serializable

@Serializable
data class InviteMemberRequest(
    val email: String,
    val role: MemberRole = MemberRole.MEMBER
)

@Serializable
data class UpdateMemberRoleRequest(
    val role: MemberRole
)

@Serializable
data class ParkingLotMemberResponse(
    val id: Long,
    val userId: String,
    val email: String,
    val name: String?,
    val role: MemberRole,
    val status: MemberStatus,
    val invitedBy: String?,
    val joinedAt: String?
)
