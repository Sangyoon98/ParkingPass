package com.sangyoon.parkingpass.parking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vehicle(
    val id: Long = 0,
    @SerialName("parking_lot_id")
    val parkingLotId: Long,
    @SerialName("plate_number")
    val plateNumber: String,
    val label: String,
    val category: VehicleCategory,
    val memo: String? = null
)
