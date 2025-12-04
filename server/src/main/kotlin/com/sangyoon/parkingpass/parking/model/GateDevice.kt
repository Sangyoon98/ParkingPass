package com.sangyoon.parkingpass.parking.model

data class GateDevice(
    val id: Long,
    val parkingLotId: Long,
    val name: String,
    val deviceKey: String,
    val direction: GateDirection
)
