package com.sangyoon.parkingpass.parking.model

import kotlinx.serialization.Serializable

@Serializable
enum class GateDirection {
    ENTER,
    EXIT,
    BOTH
}