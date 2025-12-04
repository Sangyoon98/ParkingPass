package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.Vehicle
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryVehicleRepository : VehicleRepository {
    private val idGenerator = AtomicLong(1L)
    // key: "$parkingLotId|$plateNumber"
    private val byLotAndPlate = ConcurrentHashMap<String, Vehicle>()

    override fun findByParkingLotIdAndPlateNumber(
        parkingLotId: Long,
        plateNumber: String
    ): Vehicle? = byLotAndPlate[key(parkingLotId, plateNumber)]

    override fun save(vehicle: Vehicle): Vehicle {
        val id = if (vehicle.id != 0L) vehicle.id else idGenerator.getAndIncrement()
        val saved = vehicle.copy(id = id)
        byLotAndPlate[key(saved.parkingLotId, saved.plateNumber)] = saved
        return saved
    }

    private fun key(parkingLotId: Long, plateNumber: String): String = "$parkingLotId|$plateNumber"
}