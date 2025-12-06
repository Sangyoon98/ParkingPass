package com.sangyoon.parkingpass.gate.dto

import com.sangyoon.parkingpass.parking.model.GateDirection
import kotlinx.serialization.Serializable

/**
 * 게이트 등록 요청 DTO
 *
 * @property parkingLotId 주차장 ID
 * @property name 게이트 이름 (예: "정문", "후문")
 * @property deviceKey 게이트 장비의 고유 키 (unique)
 * @property direction 게이트 방향 (ENTER, EXIT, BOTH)
 */
@Serializable
data class RegisterGateRequest(
    val parkingLotId: Long,
    val name: String,
    val deviceKey: String,
    val direction: GateDirection
)

/**
 * 게이트 응답 DTO
 */
@Serializable
data class GateResponse(
    val id: Long,
    val parkingLotId: Long,
    val name: String,
    val deviceKey: String,
    val direction: GateDirection
)