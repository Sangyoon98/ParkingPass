package com.sangyoon.parkingpass.domain.model

data class Gate(
    val id: Long,
    val parkingLotId: Long,
    val name: String,
    val deviceKey: String,
    val direction: GateDirection
)