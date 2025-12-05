package com.sangyoon.parkingpass.session.dto

import com.sangyoon.parkingpass.parking.model.VehicleCategory
import kotlinx.serialization.Serializable

/**
 * 세션 응답 DTO
 */
@Serializable
data class SessionResponse(
    val id: Long,
    val parkingLotId: Long,
    val plateNumber: String,
    val vehicleId: Long?,
    val vehicleLabel: String?,
    val vehicleCategory: VehicleCategory?,
    val enterGateId: Long,
    val exitGateId: Long?,
    val enteredAt: String,  // ISO8601 문자열
    val exitedAt: String?,  // ISO8601 문자열
    val status: String  // "OPEN" or "CLOSED"
)
