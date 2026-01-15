package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.Vehicle

interface VehicleRepository {
    suspend fun findByParkingLotIdAndPlateNumber(parkingLotId: Long, plateNumber: String): Vehicle?
    suspend fun save(vehicle: Vehicle): Vehicle
    suspend fun findAllByParkingLotId(parkingLotId: Long): List<Vehicle>
    suspend fun findById(id: Long): Vehicle?
    suspend fun update(vehicle: Vehicle): Vehicle
    suspend fun delete(id: Long): Boolean
}