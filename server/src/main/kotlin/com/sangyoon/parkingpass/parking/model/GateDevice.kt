package com.sangyoon.parkingpass.parking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GateDevice(
    val id: Long = 0,
    @SerialName("parking_lot_id")
    val parkingLotId: Long,
    val name: String,
    @SerialName("device_key")
    val deviceKey: String,
    val direction: GateDirection
)
