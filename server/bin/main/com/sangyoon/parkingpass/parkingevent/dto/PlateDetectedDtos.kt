package com.sangyoon.parkingpass.parkingevent.dto

import com.sangyoon.parkingpass.parking.model.VehicleCategory
import com.sangyoon.parkingpass.parkingevent.model.ParkingAction
import kotlinx.serialization.Serializable

/**
 * 번호판 인식 이벤트 요청 DTO.
 *
 * @property deviceKey 이벤트를 보낸 게이트 장비의 고유 키 (gate_device.device_key)
 * @property plateNumber 카메라 OCR로 인식한 번호판 문자열 (원본)
 * @property capturedAt 번호판이 인식된 시각 (ISO8601, 예: 2025-12-03T17:00:00+09:00)
 *                      생략 시 서버에서 현재 시간 사용
 */
@Serializable
data class PlateDetectedRequest(
    val deviceKey: String,
    val plateNumber: String,
    val capturedAt: String? = null // ISO8601 (e.g. 2025-12-03T17:00:00+09:00)
)

/**
 * 번호판 인식 이벤트 처리 결과 응답 DTO.
 *
 * @property action ENTER 또는 EXIT (입차/출차 판정 결과)
 * @property sessionId 생성되거나 갱신된 parking_session의 ID
 * @property plateNumber 정규화된 번호판 (공백 제거 등)
 * @property isRegistered 등록 차량 여부 (vehicle 존재 여부)
 * @property vehicleLabel 등록 차량 라벨 (예: "101동 1203호")
 * @property vehicleCategory 등록 차량 카테고리 (RESIDENT, EMPLOYEE, DELIVERY, VISITOR 등)
 */
@Serializable
data class PlateDetectedResponse(
    val action: ParkingAction,
    val sessionId: Long,
    val plateNumber: String,
    val isRegistered: Boolean,
    val vehicleLabel: String? = null,
    val vehicleCategory: VehicleCategory? = null
)