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
    suspend fun registerGate(request: RegisterGateRequest): GateResponse {
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

    suspend fun getGates(parkingLotId: Long): List<GateResponse> {
        return gateDeviceRepository.findAllByParkingLotId(parkingLotId).map { toResponse(it) }
    }

    suspend fun updateGate(id: Long, request: RegisterGateRequest): GateResponse {
        // 게이트 존재 여부 확인
        val existing = gateDeviceRepository.findById(id)
            ?: throw IllegalArgumentException("존재하지 않는 게이트입니다: $id")

        // 주차장 ID 검증
        if (existing.parkingLotId != request.parkingLotId) {
            throw IllegalArgumentException("다른 주차장의 게이트입니다")
        }

        // deviceKey 중복 체크 (자기 자신 제외)
        val duplicateCheck = gateDeviceRepository.findByDeviceKey(request.deviceKey)
        if (duplicateCheck != null && duplicateCheck.id != id) {
            throw IllegalArgumentException("이미 등록한 게이트 deviceKey입니다: ${request.deviceKey}")
        }

        val updated = existing.copy(
            name = request.name,
            deviceKey = request.deviceKey,
            direction = request.direction
        )

        val saved = gateDeviceRepository.update(updated)
        return toResponse(saved)
    }

    suspend fun deleteGate(id: Long, parkingLotId: Long) {
        // 게이트 존재 여부 확인
        val existing = gateDeviceRepository.findById(id)
            ?: throw IllegalArgumentException("존재하지 않는 게이트입니다: $id")

        // 주차장 ID 검증
        if (existing.parkingLotId != parkingLotId) {
            throw IllegalArgumentException("다른 주차장의 게이트입니다")
        }

        val success = gateDeviceRepository.delete(id)
        if (!success) {
            throw RuntimeException("게이트 삭제에 실패했습니다")
        }
    }

    private fun toResponse(gate: GateDevice): GateResponse = GateResponse(
        id = gate.id,
        parkingLotId = gate.parkingLotId,
        name = gate.name,
        deviceKey = gate.deviceKey,
        direction = gate.direction
    )
}
