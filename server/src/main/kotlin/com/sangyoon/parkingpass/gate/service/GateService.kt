package com.sangyoon.parkingpass.gate.service

import com.sangyoon.parkingpass.gate.dto.GateResponse
import com.sangyoon.parkingpass.gate.dto.RegisterGateRequest
import com.sangyoon.parkingpass.parking.model.GateDevice
import com.sangyoon.parkingpass.parking.repository.GateDeviceRepository
import com.sangyoon.parkingpass.parking.repository.ParkingLotRepository

class GateService(
    private val gateDeviceRepository: GateDeviceRepository,
    private val parkingLotRepository: ParkingLotRepository
) {
    fun registerGate(request: RegisterGateRequest): GateResponse {
        // 주차장 존재 여부 확인
        val parkingLot = parkingLotRepository.findById(request.parkingLotId)
            ?: throw IllegalArgumentException("존재하지 않는 주차장입니다: ${request.parkingLotId}")

        // 중복 체크 (deviceKey는 unique)
        val existing = gateDeviceRepository.findByDeviceKey(request.deviceKey)
        if (existing != null) {
            throw IllegalArgumentException("이미 등록한 게이트 deviceKey입니다: ${request.deviceKey}")
        }

        val gate = GateDevice(
            id = 0L,
            parkingLotId = parkingLot.id,
            name = request.name,
            deviceKey = request.deviceKey,
            direction = request.direction
        )

        val saved = gateDeviceRepository.save(gate)
        return toResponse(saved)
    }

    fun getGates(parkingLotId: Long): List<GateResponse> {
        return gateDeviceRepository.findAllByParkingLotId(parkingLotId).map { toResponse(it) }
    }

    private fun toResponse(gate: GateDevice): GateResponse = GateResponse(
        id = gate.id,
        parkingLotId = gate.parkingLotId,
        name = gate.name,
        deviceKey = gate.deviceKey,
        direction = gate.direction
    )
}