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
        direction: GateDirection
    ): Result<Gate> = runCatching {
        val response = dataSource.registerGate(
            RegisterGateRequest(parkingLotId, name, deviceKey, direction.name)
        )
        response.toDomain()
    }

    override suspend fun updateGate(
        gateId: Long,
        parkingLotId: Long,
        name: String,
        deviceKey: String,
        direction: GateDirection
    ): Result<Gate> = runCatching {
        val response = dataSource.updateGate(
            gateId,
            RegisterGateRequest(parkingLotId, name, deviceKey, direction.name)
        )
        response.toDomain()
    }

    override suspend fun deleteGate(gateId: Long, parkingLotId: Long): Result<Unit> = runCatching {
        dataSource.deleteGate(gateId, parkingLotId)
    }
}

private fun GateResponse.toDomain() = Gate(
    id = id,
    parkingLotId = parkingLotId,
    name = name,
    deviceKey = deviceKey,
    direction = runCatching { GateDirection.valueOf(direction) }
        .getOrElse { throw IllegalArgumentException("Unknown gate direction: $direction") }
)