package com.sangyoon.parkingpass.data.repository

import com.sangyoon.parkingpass.api.dto.GateResponse
import com.sangyoon.parkingpass.api.dto.RegisterGateRequest
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.domain.model.Gate
import com.sangyoon.parkingpass.domain.model.GateDirection
import com.sangyoon.parkingpass.domain.repository.GateRepository

class GateRepositoryImpl(
    private val dataSource: ParkingApiDataSource
) : GateRepository {
    override suspend fun getGates(parkingLotId: Long): Result<List<Gate>> = runCatching {
        dataSource.getGates(parkingLotId).map { it.toDomain() }
    }

    override suspend fun registerGate(
        parkingLotId: Long,
        name: String,
        deviceKey: String,
        direction: String
    ): Result<Gate> = runCatching {
        val response = dataSource.registerGate(
            RegisterGateRequest(parkingLotId, name, deviceKey, direction)
        )
        response.toDomain()
    }
}

private fun GateResponse.toDomain() = Gate(
    id = id,
    parkingLotId = parkingLotId,
    name = name,
    deviceKey = deviceKey,
    direction = GateDirection.valueOf(direction)
)