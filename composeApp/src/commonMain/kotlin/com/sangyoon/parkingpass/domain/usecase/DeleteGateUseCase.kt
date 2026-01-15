package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.repository.GateRepository

class DeleteGateUseCase(
    private val repository: GateRepository
) {
    suspend operator fun invoke(gateId: Long, parkingLotId: Long): Result<Unit> {
        if (gateId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 게이트 ID"))
        if (parkingLotId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID"))
        return repository.deleteGate(gateId, parkingLotId)
    }
}
