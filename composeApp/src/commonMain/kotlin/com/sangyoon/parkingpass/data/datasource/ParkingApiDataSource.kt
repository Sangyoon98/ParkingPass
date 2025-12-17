package com.sangyoon.parkingpass.data.datasource

import com.sangyoon.parkingpass.api.ParkingApiClient
import com.sangyoon.parkingpass.api.dto.CreateParkingLotRequest
import com.sangyoon.parkingpass.api.dto.CreateVehicleRequest
import com.sangyoon.parkingpass.api.dto.ParkingLotResponse
import com.sangyoon.parkingpass.api.dto.PlateDetectedRequest
import com.sangyoon.parkingpass.api.dto.RegisterGateRequest

class ParkingApiDataSource(
    private val apiClient: ParkingApiClient
) {
    suspend fun getParkingLots(): List<ParkingLotResponse> = apiClient.getParkingLots()

    suspend fun getParkingLot(id: Long): ParkingLotResponse = apiClient.getParkingLot(id)

    suspend fun createParkingLot(request: CreateParkingLotRequest): ParkingLotResponse = apiClient.createParkingLot(request)

    suspend fun getOpenSessions(parkingLotId: Long) = apiClient.getOpenSessions(parkingLotId)

    suspend fun getSessionHistory(parkingLotId: Long, date: String) = apiClient.getSessionHistory(parkingLotId, date)

    suspend fun createVehicle(request: CreateVehicleRequest) = apiClient.createVehicle(request)

    suspend fun getVehicles(parkingLotId: Long) = apiClient.getVehicles(parkingLotId)

    suspend fun getGates(parkingLotId: Long) = apiClient.getGates(parkingLotId)

    suspend fun registerGate(request: RegisterGateRequest) = apiClient.registerGate(request)

    suspend fun detectPlate(request: PlateDetectedRequest) = apiClient.postPlateDetected(request)
}