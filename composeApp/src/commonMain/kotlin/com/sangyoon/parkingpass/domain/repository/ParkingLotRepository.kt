package com.sangyoon.parkingpass.domain.repository

import com.sangyoon.parkingpass.domain.model.ParkingLot

interface ParkingLotRepository {
    suspend fun getParkingLots(): Result<List<ParkingLot>>
    suspend fun getParkingLot(id: Long): Result<ParkingLot>
    suspend fun createParkingLot(name: String, location: String): Result<ParkingLot>
    suspend fun getMyParkingLots(): Result<List<ParkingLot>>
    suspend fun searchParkingLots(query: String): Result<List<ParkingLot>>
}
