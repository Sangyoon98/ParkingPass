package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.Gate
import com.sangyoon.parkingpass.domain.model.PlateDetectionResult
import com.sangyoon.parkingpass.domain.model.Session
import com.sangyoon.parkingpass.domain.model.Vehicle

data class PlateDetectionUiState(
    val gates: List<Gate> = emptyList(),
    val selectedGate: Gate? = null,
    val plateNumber: String = "",
    val isLoading: Boolean = false,
    val isDetecting: Boolean = false,
    val result: PlateDetectionResult? = null,
    val error: String? = null,
    val recognizedPlate: String? = null,  // 인식된 번호 (프리뷰 오버레이용)
    val isRecognizing: Boolean = false,    // 인식 중 여부
    val vehicleInfo: VehicleInfo? = null,  // 조회된 차량 정보
    val showVehicleSheet: Boolean = false  // 바텀시트 표시 여부
)

/**
 * 차량 정보 및 현재 주차 세션 정보
 */
data class VehicleInfo(
    val plateNumber: String,
    val vehicle: Vehicle?,  // null이면 미등록
    val currentSession: Session?  // 현재 주차 세션 (있으면 주차 중)
)
