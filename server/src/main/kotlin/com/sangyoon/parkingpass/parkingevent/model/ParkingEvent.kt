package com.sangyoon.parkingpass.parkingevent.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParkingEvent(
    val id: Long = 0,
    @SerialName("device_key")
    val deviceKey: String,
    @SerialName("plate_number")
    val plateNumber: String,
    val action: ParkingAction
)