package com.sangyoon.parkingpass.domain.repository

import com.sangyoon.parkingpass.domain.model.Vehicle
import com.sangyoon.parkingpass.domain.model.VehicleCategory

interface VehicleRepository {
    suspend fun createVehicle(
        parkingLotId: Long,
        plateNumber: String,
        label: String,
        category: VehicleCategory,
        memo: String?
    ): Result<Vehicle>
    suspend fun getVehicles(parkingLotId: Long): Result<List<Vehicle>>
    suspend fun getVehicleByPlateNumber(parkingLotId: Long, plateNumber: String): Result<Vehicle?>
    suspend fun updateVehicle(
        vehicleId: Long,
        parkingLotId: Long,
        plateNumber: String,
        label: String,
        category: VehicleCategory,
        memo: String?
    ): Result<Vehicle>
    suspend fun deleteVehicle(vehicleId: Long, parkingLotId: Long): Result<Unit>
}