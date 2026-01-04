package com.sangyoon.parkingpass.domain.model

data class ParkingLotMember(
    val id: Long,
    val userId: String,
    val email: String,
    val name: String?,
    val role: String,
    val status: String,
    val invitedBy: String?,
    val joinedAt: String?
)
