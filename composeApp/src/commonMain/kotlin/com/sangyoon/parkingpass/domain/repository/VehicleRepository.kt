package com.sangyoon.parkingpass.domain.repository

import com.sangyoon.parkingpass.domain.model.Vehicle

interface VehicleRepository {
    suspend fun createVehicle(
        parkingLotId: Long,
        plateNumber: String,
        label: String,
        category: String,
        memo: String?
    ): Result<Vehicle>
    suspend fun getVehicles(parkingLotId: Long): Result<List<Vehicle>>
}