package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.Vehicle
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryVehicleRepository : VehicleRepository {
    private val idGenerator = AtomicLong(1L)
    // key: "$parkingLotId|$plateNumber"
    private val byLotAndPlate = ConcurrentHashMap<String, Vehicle>()
    private val byId = ConcurrentHashMap<Long, Vehicle>()

    override suspend fun findByParkingLotIdAndPlateNumber(
        parkingLotId: Long,
        plateNumber: String
    ): Vehicle? = byLotAndPlate[key(parkingLotId, plateNumber)]

    override suspend fun save(vehicle: Vehicle): Vehicle {
        val id = if (vehicle.id != 0L) vehicle.id else idGenerator.getAndIncrement()
        val saved = vehicle.copy(id = id)
        byLotAndPlate[key(saved.parkingLotId, saved.plateNumber)] = saved
        byId[id] = saved
        return saved
    }

    override suspend fun findAllByParkingLotId(parkingLotId: Long): List<Vehicle> {
        return byLotAndPlate.values.filter { it.parkingLotId == parkingLotId }
    }

    override suspend fun findById(id: Long): Vehicle? = byId[id]

    override suspend fun update(vehicle: Vehicle): Vehicle {
        byId[vehicle.id] = vehicle
        byLotAndPlate[key(vehicle.parkingLotId, vehicle.plateNumber)] = vehicle
        return vehicle
    }

    override suspend fun delete(id: Long): Boolean {
        val vehicle = byId.remove(id)
        if (vehicle != null) {
            byLotAndPlate.remove(key(vehicle.parkingLotId, vehicle.plateNumber))
            return true
        }
        return false
    }

    private fun key(parkingLotId: Long, plateNumber: String): String = "$parkingLotId|$plateNumber"
}