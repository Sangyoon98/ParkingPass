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
        category: String,
        memo: String?
    ): Result<Vehicle> = runCatching {
        val response = dataSource.createVehicle(
            CreateVehicleRequest(parkingLotId, plateNumber, label, category, memo)
        )
        response.toDomain()
    }

    override suspend fun getVehicles(parkingLotId: Long): Result<List<Vehicle>> = runCatching {
        dataSource.getVehicles(parkingLotId).map { it.toDomain() }
    }
}

private fun VehicleResponse.toDomain() = Vehicle(
    id = id,
    parkingLotId = parkingLotId,
    plateNumber = plateNumber,
    label = label,
    category = VehicleCategory.valueOf(category),
    memo = memo
)