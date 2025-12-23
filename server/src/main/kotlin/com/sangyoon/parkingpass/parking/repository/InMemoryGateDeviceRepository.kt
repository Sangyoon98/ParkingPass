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
}