package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.ParkingLot
import com.sangyoon.parkingpass.domain.repository.ParkingLotRepository

class GetParkingLotDetailUseCase(
    private val repository: ParkingLotRepository
) {
    suspend operator fun invoke(id: Long): Result<ParkingLot> {
        if (id <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID입니다"))
        return repository.getParkingLot(id)
    }
}