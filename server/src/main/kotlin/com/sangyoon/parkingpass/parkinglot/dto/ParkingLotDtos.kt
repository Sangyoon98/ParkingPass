package com.sangyoon.parkingpass.parkinglot.dto

import kotlinx.serialization.Serializable

/**
 * 주차장 등록 요청 DTO
 *
 * @property name 주차장 이름
 * @property location 주차장 위치
 */
@Serializable
data class CreateParkingLotRequest(
    val name: String,
    val location: String
)

/**
 * 주차장 응답 DTO
 */
@Serializable
data class ParkingLotResponse(
    val id: Long,
    val name: String,
    val location: String
)