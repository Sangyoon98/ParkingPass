package com.sangyoon.parkingpass.domain.repository

import com.sangyoon.parkingpass.domain.model.Vehicle
import com.sangyoon.parkingpass.domain.model.VehicleCategory

interface VehicleRepository {
    suspend fun createVehicle(
        parkingLotId: Long,
        plateNumber: String,
        label: String,
        category: VehicleCategory,
        memo: String?
    ): Result<Vehicle>
    suspend fun getVehicles(parkingLotId: Long): Result<List<Vehicle>>
    suspend fun getVehicleByPlateNumber(parkingLotId: Long, plateNumber: String): Result<Vehicle?>
    /**
     * 차량 정보를 수정합니다.
     *
     * 주의: 차량 번호판(plateNumber)은 수정할 수 없습니다.
     * 차량 번호판을 변경해야 하는 경우 차량을 삭제하고 새로 등록해야 합니다.
     *
     * @param vehicleId 수정할 차량 ID
     * @param parkingLotId 주차장 ID (권한 검증용)
     * @param label 차량 이름
     * @param category 차량 종류
     * @param memo 메모 (선택)
     * @return 수정된 차량 정보
     */
    suspend fun updateVehicle(
        vehicleId: Long,
        parkingLotId: Long,
        label: String,
        category: VehicleCategory,
        memo: String?
    ): Result<Vehicle>
    suspend fun deleteVehicle(vehicleId: Long, parkingLotId: Long): Result<Unit>
}