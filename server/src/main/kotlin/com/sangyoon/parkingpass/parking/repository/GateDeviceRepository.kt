package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.GateDevice

interface GateDeviceRepository {
    suspend fun findByDeviceKey(deviceKey: String): GateDevice?
    suspend fun save(device: GateDevice): GateDevice
    suspend fun findAllByParkingLotId(parkingLotId: Long): List<GateDevice>
}