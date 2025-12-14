package com.sangyoon.parkingpass.domain.model

data class Vehicle(
    val id: Long,
    val parkingLotId: Long,
    val plateNumber: String,
    val label: String,
    val category: VehicleCategory,
    val memo: String?
)
