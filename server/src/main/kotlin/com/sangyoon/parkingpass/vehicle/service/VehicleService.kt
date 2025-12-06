package com.sangyoon.parkingpass.vehicle.service

import com.sangyoon.parkingpass.parking.model.Vehicle
import com.sangyoon.parkingpass.parking.repository.ParkingLotRepository
import com.sangyoon.parkingpass.parking.repository.VehicleRepository
import com.sangyoon.parkingpass.vehicle.dto.CreateVehicleRequest
import com.sangyoon.parkingpass.vehicle.dto.VehicleResponse

class VehicleService(
    private val vehicleRepository: VehicleRepository,
    private val parkingLotRepository: ParkingLotRepository
) {
    fun createVehicle(request: CreateVehicleRequest): VehicleResponse {
        // 주차장 존재 여부 확인
        val parkingLot = parkingLotRepository.findById(request.parkingLotId)
            ?: throw IllegalArgumentException("존재하지 않는 주차장입니다: ${request.parkingLotId}")

        // 중복 체크
        val existing = vehicleRepository.findByParkingLotIdAndPlateNumber(
            parkingLotId = request.parkingLotId,
            plateNumber = normalizePlateNumber(request.plateNumber)
        )

        if (existing != null) {
            throw IllegalArgumentException("이미 등록된 차량입니다: ${request.plateNumber}")
        }

        val vehicle = Vehicle(
            id = 0L,
            parkingLotId = parkingLot.id,
            plateNumber = normalizePlateNumber(request.plateNumber),
            label = request.label,
            category = request.category,
            memo = request.memo
        )

        val saved = vehicleRepository.save(vehicle)
        return toResponse(saved)
    }

    fun getVehicles(parkingLotId: Long): List<VehicleResponse> {
        return vehicleRepository.findAllByParkingLotId(parkingLotId)
            .map { toResponse(it) }
    }

    private fun normalizePlateNumber(plate: String): String =
        plate.replace(" ", "")

    private fun toResponse(vehicle: Vehicle): VehicleResponse =
        VehicleResponse(
            id = vehicle.id,
            parkingLotId = vehicle.parkingLotId,
            plateNumber = vehicle.plateNumber,
            label = vehicle.label,
            category = vehicle.category,
            memo = vehicle.memo
        )
}