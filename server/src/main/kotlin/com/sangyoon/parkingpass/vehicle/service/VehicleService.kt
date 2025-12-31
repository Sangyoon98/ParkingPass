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
    suspend fun createVehicle(request: CreateVehicleRequest): VehicleResponse {
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

    suspend fun getVehicles(parkingLotId: Long): List<VehicleResponse> {
        return try {
            vehicleRepository.findAllByParkingLotId(parkingLotId)
                .map { toResponse(it) }
        } catch (e: Exception) {
            // Supabase 조회 에러 등은 UI에 500을 띄우지 않고 "빈 목록"으로 처리
            emptyList()
        }
    }

    suspend fun getVehicleByPlateNumber(parkingLotId: Long, plateNumber: String): VehicleResponse? {
        return try {
            val normalizedPlate = normalizePlateNumber(plateNumber)
            println("🔍 [VehicleService] 차량 조회 - parkingLotId: $parkingLotId, 원본: '$plateNumber', 정규화: '$normalizedPlate'")
            val vehicle = vehicleRepository.findByParkingLotIdAndPlateNumber(parkingLotId, normalizedPlate)
            if (vehicle != null) {
                println("✅ [VehicleService] 차량 찾음: id=${vehicle.id}, plateNumber=${vehicle.plateNumber}")
            } else {
                println("❌ [VehicleService] 차량을 찾을 수 없음")
            }
            vehicle?.let { toResponse(it) }
        } catch (e: Exception) {
            println("💥 [VehicleService] 차량 조회 예외: ${e.message}")
            e.printStackTrace()
            null
        }
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