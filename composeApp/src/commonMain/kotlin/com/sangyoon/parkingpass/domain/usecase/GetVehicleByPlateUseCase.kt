package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.Vehicle
import com.sangyoon.parkingpass.domain.repository.VehicleRepository

class GetVehicleByPlateUseCase(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(parkingLotId: Long, plateNumber: String): Result<Vehicle?> {
        if (parkingLotId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID"))
        if (plateNumber.isBlank()) return Result.failure(IllegalArgumentException("번호판 번호는 필수입니다"))
        return repository.getVehicleByPlateNumber(parkingLotId, plateNumber)
    }
}

