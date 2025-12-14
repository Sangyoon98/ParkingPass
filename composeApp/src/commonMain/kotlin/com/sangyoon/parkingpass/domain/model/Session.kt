package com.sangyoon.parkingpass.domain.model

data class Session(
    val id: Long,
    val parkingLotId: Long,
    val plateNumber: String,
    val vehicleId: Long?,
    val vehicleLabel: String?,
    val vehicleCategory: VehicleCategory?,
    val enterGateId: Long,
    val exitGateId: Long?,
    val enteredAt: String,
    val exitedAt: String?,
    val status: SessionStatus
)