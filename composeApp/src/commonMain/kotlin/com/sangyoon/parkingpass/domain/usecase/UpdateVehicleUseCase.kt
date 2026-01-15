package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.Vehicle
import com.sangyoon.parkingpass.domain.model.VehicleCategory
import com.sangyoon.parkingpass.domain.repository.VehicleRepository

class UpdateVehicleUseCase(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(
        vehicleId: Long,
        parkingLotId: Long,
        plateNumber: String,
        label: String,
        category: VehicleCategory,
        memo: String?
    ): Result<Vehicle> {
        if (vehicleId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 차량 ID"))
        if (parkingLotId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID"))
        if (plateNumber.isBlank()) return Result.failure(IllegalArgumentException("차량 번호는 필수입니다"))
        if (label.isBlank()) return Result.failure(IllegalArgumentException("차량 이름은 필수입니다"))

        return repository.updateVehicle(vehicleId, parkingLotId, plateNumber, label, category, memo)
    }
}
