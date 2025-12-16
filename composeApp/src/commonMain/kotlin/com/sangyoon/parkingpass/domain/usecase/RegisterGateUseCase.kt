package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.Gate
import com.sangyoon.parkingpass.domain.repository.GateRepository

class RegisterGateUseCase(
    private val repository: GateRepository
) {
    suspend operator fun invoke(
        parkingLotId: Long,
        name: String,
        deviceKey: String,
        direction: String
    ): Result<Gate> {
        if (parkingLotId <= 0) {
            return Result.failure(IllegalArgumentException("유효하지 않은 주차장 ID입니다"))
        }
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("게이트 이름은 필수입니다"))
        }
        if (deviceKey.isBlank()) {
            return Result.failure(IllegalArgumentException("디바이스 키는 필수입니다"))
        }
        if (direction.isBlank()) {
            return Result.failure(IllegalArgumentException("방향은 필수입니다"))
        }
        val validDirections = listOf("ENTER", "EXIT", "BOTH")
        if (direction !in validDirections) {
            return Result.failure(IllegalArgumentException("방향은 ENTER, EXIT, BOTH 중 하나여야 합니다"))
        }
        return repository.registerGate(parkingLotId, name, deviceKey, direction)
    }
}