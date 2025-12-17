package com.sangyoon.parkingpass.domain.repository

import com.sangyoon.parkingpass.api.dto.PlateDetectedResponse

interface PlateDetectionRepository {
    suspend fun detectPlate(
        deviceKey: String,
        plateNumber: String,
        capturedAt: String?
    ): Result<PlateDetectedResponse>
}