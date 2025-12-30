package com.sangyoon.parkingpass.data.repository

import com.sangyoon.parkingpass.api.dto.SessionResponse
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.domain.model.Session
import com.sangyoon.parkingpass.domain.model.SessionStatus
import com.sangyoon.parkingpass.domain.model.VehicleCategory
import com.sangyoon.parkingpass.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val dataSource: ParkingApiDataSource
) : SessionRepository {

    override suspend fun getOpenSessions(parkingLotId: Long): Result<List<Session>> = runCatching {
        dataSource.getOpenSessions(parkingLotId).map { it.toDomain() }
    }

    override suspend fun getSessionHistory(parkingLotId: Long, date: String): Result<List<Session>> = runCatching {
        dataSource.getSessionHistory(parkingLotId, date).map { it.toDomain() }
    }

    override suspend fun getCurrentSessionByPlateNumber(parkingLotId: Long, plateNumber: String): Result<Session?> = runCatching {
        dataSource.getCurrentSessionByPlateNumber(parkingLotId, plateNumber)?.toDomain()
    }
}

private fun SessionResponse.toDomain() = Session(
    id = id,
    parkingLotId = parkingLotId,
    plateNumber = plateNumber,
    vehicleId = vehicleId,
    vehicleLabel = vehicleLabel,
    vehicleCategory = vehicleCategory?.let {
        runCatching { VehicleCategory.valueOf(it) }.getOrNull()
    },
    enterGateId = enterGateId,
    exitGateId = exitGateId,
    enteredAt = enteredAt,
    exitedAt = exitedAt,
    status = runCatching { SessionStatus.valueOf(status) }.getOrElse { SessionStatus.OPEN }
)