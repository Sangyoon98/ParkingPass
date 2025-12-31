package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.Session
import com.sangyoon.parkingpass.domain.repository.SessionRepository

class GetCurrentSessionByPlateUseCase(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(parkingLotId: Long, plateNumber: String): Result<Session?> {
        if (parkingLotId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID"))
        if (plateNumber.isBlank()) return Result.failure(IllegalArgumentException("번호판 번호는 필수입니다"))
        return repository.getCurrentSessionByPlateNumber(parkingLotId, plateNumber)
    }
}

