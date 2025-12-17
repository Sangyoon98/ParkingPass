package com.sangyoon.parkingpass.data.repository

import com.sangyoon.parkingpass.api.dto.PlateDetectedRequest
import com.sangyoon.parkingpass.api.dto.PlateDetectedResponse
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.domain.model.PlateDetectionAction
import com.sangyoon.parkingpass.domain.model.PlateDetectionResult
import com.sangyoon.parkingpass.domain.model.VehicleCategory
import com.sangyoon.parkingpass.domain.repository.PlateDetectionRepository

class PlateDetectionRepositoryImpl(
    private val dataSource: ParkingApiDataSource
): PlateDetectionRepository {

    override suspend fun detectPlate(
        deviceKey: String,
        plateNumber: String,
        capturedAt: String?
    ): Result<PlateDetectionResult> = runCatching {
        val response = dataSource.detectPlate(PlateDetectedRequest(deviceKey, plateNumber, capturedAt))
        response.toDomain()
    }
}

private fun PlateDetectedResponse.toDomain(): PlateDetectionResult {
    return PlateDetectionResult(
        action = runCatching { PlateDetectionAction.valueOf(action) }.getOrElse { PlateDetectionAction.ENTER },
        sessionId = sessionId,
        plateNumber = plateNumber,
        isRegistered = isRegistered,
        vehicleLabel = vehicleLabel,
        vehicleCategory = vehicleCategory?.let {
            runCatching { VehicleCategory.valueOf(it) }.getOrNull()
        }
    )
}