package com.sangyoon.parkingpass.vehicle.dto

import com.sangyoon.parkingpass.parking.model.VehicleCategory
import kotlinx.serialization.Serializable

/**
 * 등록 차량 추가 요청 DTO
 *
 * @property parkingLotId 주차장 ID
 * @property plateNumber 번호판 (정규화된 형태)
 * @property label 차량 라벨 (예: "101동 1203호")
 * @property category 차량 카테고리 (RESIDENT, EMPLOYEE, DELIVERY, VISITOR)
 * @property memo 메모 (선택사항)
 */
@Serializable
data class CreateVehicleRequest(
    val parkingLotId: Long,
    val plateNumber: String,
    val label: String,
    val category: VehicleCategory,
    val memo: String? = null
)

/**
 * 차량 응답 DTO
 */
@Serializable
data class VehicleResponse(
    val id: Long,
    val parkingLotId: Long,
    val plateNumber: String,
    val label: String,
    val category: VehicleCategory,
    val memo: String?
)