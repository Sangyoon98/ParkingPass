package com.sangyoon.parkingpass.data.repository

import com.sangyoon.parkingpass.api.dto.CreateVehicleRequest
import com.sangyoon.parkingpass.api.dto.VehicleResponse
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.domain.model.Vehicle
import com.sangyoon.parkingpass.domain.model.VehicleCategory
import com.sangyoon.parkingpass.domain.repository.VehicleRepository

class VehicleRepositoryImpl(
    private val dataSource: ParkingApiDataSource
) : VehicleRepository {

    override suspend fun createVehicle(
        parkingLotId: Long,
        plateNumber: String,
        label: String,
        category: VehicleCategory,
        memo: String?
    ): Result<Vehicle> = runCatching {
        val response = dataSource.createVehicle(
            CreateVehicleRequest(parkingLotId, plateNumber, label, category.name, memo)
        )
        response.toDomain()
    }

    override suspend fun getVehicles(parkingLotId: Long): Result<List<Vehicle>> = runCatching {
        dataSource.getVehicles(parkingLotId).map { it.toDomain() }
    }

    override suspend fun getVehicleByPlateNumber(parkingLotId: Long, plateNumber: String): Result<Vehicle?> = runCatching {
        dataSource.getVehicleByPlateNumber(parkingLotId, plateNumber)?.toDomain()
    }

    override suspend fun updateVehicle(
        vehicleId: Long,
        parkingLotId: Long,
        plateNumber: String,
        label: String,
        category: VehicleCategory,
        memo: String?
    ): Result<Vehicle> = runCatching {
        val response = dataSource.updateVehicle(
            vehicleId,
            CreateVehicleRequest(parkingLotId, plateNumber, label, category.name, memo)
        )
        response.toDomain()
    }

    override suspend fun deleteVehicle(vehicleId: Long, parkingLotId: Long): Result<Unit> = runCatching {
        dataSource.deleteVehicle(vehicleId, parkingLotId)
    }
}

private fun VehicleResponse.toDomain() = Vehicle(
    id = id,
    parkingLotId = parkingLotId,
    plateNumber = plateNumber,
    label = label,
    category = runCatching { VehicleCategory.valueOf(category) }.getOrElse { VehicleCategory.SEDAN },
    memo = memo
)