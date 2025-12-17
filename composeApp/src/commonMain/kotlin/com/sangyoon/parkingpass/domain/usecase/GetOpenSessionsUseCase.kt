package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.Session
import com.sangyoon.parkingpass.domain.repository.SessionRepository

class GetOpenSessionsUseCase(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(parkingLotId: Long): Result<List<Session>> {
        if (parkingLotId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID"))
        return repository.getOpenSessions(parkingLotId)
    }
}