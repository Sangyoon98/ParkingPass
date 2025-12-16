package com.sangyoon.parkingpass.domain.repository

import com.sangyoon.parkingpass.domain.model.Gate

interface GateRepository {
    suspend fun getGates(parkingLotId: Long): Result<List<Gate>>
    suspend fun registerGate(
        parkingLotId: Long,
        name: String,
        deviceKey: String,
        direction: String
    ): Result<Gate>
}