package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.GateDevice

interface GateDeviceRepository {
    fun findByDeviceKey(deviceKey: String): GateDevice?
    fun save(device: GateDevice): GateDevice
    fun findAllByParkingLotId(parkingLotId: Long): List<GateDevice>
}