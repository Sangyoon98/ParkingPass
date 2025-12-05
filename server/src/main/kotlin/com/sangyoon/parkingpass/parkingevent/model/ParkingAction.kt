package com.sangyoon.parkingpass.parkingevent.model

import kotlinx.serialization.Serializable

@Serializable
enum class ParkingAction {
    ENTER,
    EXIT
}