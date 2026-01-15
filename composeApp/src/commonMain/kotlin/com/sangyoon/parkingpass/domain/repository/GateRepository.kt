package com.sangyoon.parkingpass.domain.repository

import com.sangyoon.parkingpass.domain.model.Gate
import com.sangyoon.parkingpass.domain.model.GateDirection

interface GateRepository {
    suspend fun getGates(parkingLotId: Long): Result<List<Gate>>
    suspend fun registerGate(
        parkingLotId: Long,
        name: String,
        deviceKey: String,
        direction: GateDirection
    ): Result<Gate>
    suspend fun updateGate(
        gateId: Long,
        parkingLotId: Long,
        name: String,
        deviceKey: String,
        direction: GateDirection
    ): Result<Gate>
    suspend fun deleteGate(gateId: Long, parkingLotId: Long): Result<Unit>
}