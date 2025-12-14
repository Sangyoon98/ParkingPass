package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.ParkingLot
import com.sangyoon.parkingpass.domain.repository.ParkingLotRepository

class CreateParkingLotUseCase(
    private val repository: ParkingLotRepository
) {
    suspend operator fun invoke(name: String, location: String): Result<ParkingLot> {
        // 도메인 검증 로직
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("주차장 이름은 필수입니다"))
        }

        if (location.isBlank()) {
            return Result.failure(IllegalArgumentException("주차장 위치는 필수입니다"))
        }

        return repository.createParkingLot(name, location)
    }
}