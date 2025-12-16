package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.Gate
import com.sangyoon.parkingpass.domain.repository.GateRepository

class GetGatesUseCase(
    private val repository: GateRepository
) {
    suspend operator fun invoke(parkingLotId: Long): Result<List<Gate>> {
        if (parkingLotId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID입니다"))
        return repository.getGates(parkingLotId)
    }
}