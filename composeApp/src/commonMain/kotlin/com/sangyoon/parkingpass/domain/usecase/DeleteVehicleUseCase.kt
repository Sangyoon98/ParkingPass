package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.repository.VehicleRepository

class DeleteVehicleUseCase(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(vehicleId: Long): Result<Unit> {
        if (vehicleId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 차량 ID"))
        return repository.deleteVehicle(vehicleId)
    }
}
