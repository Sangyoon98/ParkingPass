package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.Vehicle
import com.sangyoon.parkingpass.domain.model.VehicleCategory
import com.sangyoon.parkingpass.domain.repository.VehicleRepository

class CreateVehicleUseCase(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(
        parkingLotId: Long,
        plateNumber: String,
        label: String,
        category: VehicleCategory,
        memo: String?
    ): Result<Vehicle> {
        if (parkingLotId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID"))
        if (plateNumber.isBlank()) return Result.failure(IllegalArgumentException("차량 번호는 필수"))
        if (label.isBlank()) return Result.failure(IllegalArgumentException("차량 라벨은 필수"))
        return repository.createVehicle(parkingLotId, plateNumber, label, category, memo)
    }
}