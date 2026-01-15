package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.GateDevice
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryGateDeviceRepository : GateDeviceRepository {
    private val idGenerator = AtomicLong(1L)
    private val byId = ConcurrentHashMap<Long, GateDevice>()
    private val byDeviceKey = ConcurrentHashMap<String, GateDevice>()

    override suspend fun findByDeviceKey(deviceKey: String): GateDevice? = byDeviceKey[deviceKey]

    override suspend fun save(device: GateDevice): GateDevice {
        val id = if (device.id != 0L) device.id else idGenerator.getAndIncrement()
        val saved = device.copy(id = id)
        byId[id] = saved
        byDeviceKey[saved.deviceKey] = saved
        return saved
    }

    override suspend fun findAllByParkingLotId(parkingLotId: Long): List<GateDevice> {
        return byId.values.filter { it.parkingLotId == parkingLotId }
    }

    override suspend fun findById(id: Long): GateDevice? = byId[id]

    override suspend fun update(device: GateDevice): GateDevice {
        val existing = byId[device.id]
        if (existing != null && existing.deviceKey != device.deviceKey) {
            byDeviceKey.remove(existing.deviceKey)
        }
        byId[device.id] = device
        byDeviceKey[device.deviceKey] = device
        return device
    }

    override suspend fun delete(id: Long): Boolean {
        val device = byId.remove(id)
        if (device != null) {
            byDeviceKey.remove(device.deviceKey)
            return true
        }
        return false
    }
}