package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.Vehicle

interface VehicleRepository {
    fun findByParkingLotIdAndPlateNumber(parkingLotId: Long, plateNumber: String): Vehicle?
    fun save(vehicle: Vehicle): Vehicle
}