package com.sangyoon.parkingpass.domain.model

data class PlateDetectionResult(
    val action: PlateDetectionAction,
    val sessionId: Long,
    val plateNumber: String,
    val isRegistered: Boolean,
    val vehicleLabel: String?,
    val vehicleCategory: VehicleCategory?
)