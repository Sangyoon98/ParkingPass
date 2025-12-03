package com.sangyoon.parkingpass.parkingevent.dto

import com.sangyoon.parkingpass.parkingevent.model.ParkingAction
import kotlinx.serialization.Serializable

@Serializable
data class PlateDetectedRequest(
    val deviceKey: String,
    val plateNumber: String
)

@Serializable
data class PlateDetectedResponse(
    val id: Long,
    val action: ParkingAction,
    val plateNumber: String
)