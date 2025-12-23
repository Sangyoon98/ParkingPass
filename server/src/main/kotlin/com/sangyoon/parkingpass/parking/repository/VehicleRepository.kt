package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.Vehicle

interface VehicleRepository {
    suspend fun findByParkingLotIdAndPlateNumber(parkingLotId: Long, plateNumber: String): Vehicle?
    suspend fun save(vehicle: Vehicle): Vehicle
    suspend fun findAllByParkingLotId(parkingLotId: Long): List<Vehicle>
}