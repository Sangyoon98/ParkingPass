package com.sangyoon.parkingpass.data.repository

import com.sangyoon.parkingpass.api.dto.PlateDetectedRequest
import com.sangyoon.parkingpass.api.dto.PlateDetectedResponse
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.domain.repository.PlateDetectionRepository

class PlateDetectionRepositoryImpl(
    private val dataSource: ParkingApiDataSource
): PlateDetectionRepository {

    override suspend fun detectPlate(
        deviceKey: String,
        plateNumber: String,
        capturedAt: String?
    ): Result<PlateDetectedResponse> = runCatching {
        dataSource.detectPlate(PlateDetectedRequest(deviceKey, plateNumber, capturedAt))
    }
}