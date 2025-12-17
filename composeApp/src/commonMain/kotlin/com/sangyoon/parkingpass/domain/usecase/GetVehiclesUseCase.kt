package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.Vehicle
import com.sangyoon.parkingpass.domain.repository.VehicleRepository

class GetVehiclesUseCase(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(parkingLotId: Long): Result<List<Vehicle>> {
        if (parkingLotId <= 0) return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID"))
        return repository.getVehicles(parkingLotId)
    }
}