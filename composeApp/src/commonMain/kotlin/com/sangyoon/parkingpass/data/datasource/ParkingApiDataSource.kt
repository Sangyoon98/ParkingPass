package com.sangyoon.parkingpass.data.datasource

import com.sangyoon.parkingpass.api.ParkingApiClient
import com.sangyoon.parkingpass.api.dto.CreateParkingLotRequest
import com.sangyoon.parkingpass.api.dto.ParkingLotResponse

class ParkingApiDataSource(
    private val apiClient: ParkingApiClient
) {
    suspend fun getParkingLots(): List<ParkingLotResponse> {
        return apiClient.getParkingLots()
    }

    suspend fun getParkingLot(id: Long): ParkingLotResponse {
        return apiClient.getParkingLot(id)
    }

    suspend fun createParkingLot(request: CreateParkingLotRequest): ParkingLotResponse {
        return apiClient.createParkingLot(request)
    }
}