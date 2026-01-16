package com.sangyoon.parkingpass.domain.model

data class ParkingLot(
    val id: Long,
    val name: String,
    val location: String,
    val ownerId: String? = null,
    val isPublic: Boolean = true,
    val joinCode: String? = null,
    val capacity: Int = 100
)
