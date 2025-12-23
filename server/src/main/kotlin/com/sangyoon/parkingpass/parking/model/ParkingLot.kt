package com.sangyoon.parkingpass.parking.model

import kotlinx.serialization.Serializable

@Serializable
data class ParkingLot(
    val id: Long = 0,
    val name: String,
    val location: String
)