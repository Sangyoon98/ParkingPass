package com.sangyoon.parkingpass.data.repository

import com.sangyoon.parkingpass.api.dto.CreateParkingLotRequest
import com.sangyoon.parkingpass.api.dto.ParkingLotResponse
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.domain.model.ParkingLot
import com.sangyoon.parkingpass.domain.repository.ParkingLotRepository

class ParkingLotRepositoryImpl(
    private val dataSource: ParkingApiDataSource
) : ParkingLotRepository {
    override suspend fun getParkingLots(): Result<List<ParkingLot>> {
        return try {
            val response = dataSource.getParkingLots()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getParkingLot(id: Long): Result<ParkingLot> {
        return try {
            val response = dataSource.getParkingLot(id)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createParkingLot(
        name: String,
        location: String
    ): Result<ParkingLot> {
        return try {
            val request = CreateParkingLotRequest(name, location)
            val response = dataSource.createParkingLot(request)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun ParkingLotResponse.toDomain(): ParkingLot {
    return ParkingLot(
        id = id,
        name = name,
        location = location,
        ownerId = ownerId,
        isPublic = isPublic,
        joinCode = joinCode
    )
}
