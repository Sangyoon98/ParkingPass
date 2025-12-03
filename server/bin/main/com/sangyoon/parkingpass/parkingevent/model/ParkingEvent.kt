package com.sangyoon.parkingpass.parkingevent.model

data class ParkingEvent(
    val id: Long,
    val deviceKey: String,
    val plateNumber: String,
    val action: ParkingAction
)