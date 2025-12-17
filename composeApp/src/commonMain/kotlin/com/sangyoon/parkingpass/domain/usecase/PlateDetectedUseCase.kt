package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.api.dto.PlateDetectedResponse
import com.sangyoon.parkingpass.domain.repository.PlateDetectionRepository

class PlateDetectedUseCase(
    private val repository: PlateDetectionRepository
) {
    suspend operator fun invoke(
        deviceKey: String,
        plateNumber: String,
        capturedAt: String? = null
    ): Result<PlateDetectedResponse> {
        if (deviceKey.isBlank()) return Result.failure(IllegalArgumentException("디바이스 키는 필수입니다"))
        if (plateNumber.isBlank()) return Result.failure(IllegalArgumentException("번호판 번호는 필수입니다"))
        return repository.detectPlate(deviceKey, plateNumber, capturedAt)
    }
}