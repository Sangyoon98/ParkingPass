package com.sangyoon.parkingpass.domain.repository

import com.sangyoon.parkingpass.domain.model.PlateDetectionResult

interface PlateDetectionRepository {
    suspend fun detectPlate(
        deviceKey: String,
        plateNumber: String,
        capturedAt: String?
    ): Result<PlateDetectionResult>
}