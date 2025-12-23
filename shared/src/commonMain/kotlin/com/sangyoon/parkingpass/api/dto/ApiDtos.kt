package com.sangyoon.parkingpass.api.dto

import kotlinx.serialization.Serializable

// 번호판 인식 관련
@Serializable
data class PlateDetectedRequest(
    val deviceKey: String,
    val plateNumber: String,
    val capturedAt: String? = null
)

@Serializable
data class PlateDetectedResponse(
    val action: String,  // "ENTER" or "EXIT"
    val sessionId: Long,
    val plateNumber: String,
    val isRegistered: Boolean,
    val vehicleLabel: String? = null,
    val vehicleCategory: String? = null  // "RESIDENT", "EMPLOYEE", etc.
)

// 주차장 관련
@Serializable
data class CreateParkingLotRequest(
    val name: String,
    val location: String
)

@Serializable
data class ParkingLotResponse(
    val id: Long,
    val name: String,
    val location: String
)

// 게이트 관련
@Serializable
data class RegisterGateRequest(
    val parkingLotId: Long,
    val name: String,
    val deviceKey: String,
    val direction: String  // "ENTER", "EXIT", "BOTH"
)

@Serializable
data class GateResponse(
    val id: Long,
    val parkingLotId: Long,
    val name: String,
    val deviceKey: String,
    val direction: String
)

// 차량 관련
@Serializable
data class CreateVehicleRequest(
    val parkingLotId: Long,
    val plateNumber: String,
    val label: String,
    val category: String,  // "RESIDENT", "EMPLOYEE", etc.
    val memo: String? = null
)

@Serializable
data class VehicleResponse(
    val id: Long,
    val parkingLotId: Long,
    val plateNumber: String,
    val label: String,
    val category: String,
    val memo: String?
)

// 세션 관련
@Serializable
data class SessionResponse(
    val id: Long,
    val parkingLotId: Long,
    val plateNumber: String,
    val vehicleId: Long?,
    val vehicleLabel: String?,
    val vehicleCategory: String?,
    val enterGateId: Long,
    val exitGateId: Long?,
    val enteredAt: String,
    val exitedAt: String?,
    val status: String  // "OPEN" or "CLOSED"
)

// 공통 에러 응답 (서버의 ErrorResponse와 동일한 구조)
@Serializable
data class ErrorResponseDto(
    val code: String,
    val message: String
)