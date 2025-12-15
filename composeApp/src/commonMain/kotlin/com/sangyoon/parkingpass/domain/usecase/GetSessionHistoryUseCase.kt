package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.Session
import com.sangyoon.parkingpass.domain.repository.SessionRepository

class GetSessionHistoryUseCase(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(parkingLotId: Long, date: String): Result<List<Session>> {
        if (parkingLotId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID"))
        if (date.isBlank()) return Result.failure(IllegalArgumentException("date는 YYYY-MM-DD 형식이어야 합니다"))
        return repository.getSessionHistory(parkingLotId, date)
    }
}