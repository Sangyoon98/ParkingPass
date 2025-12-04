package com.sangyoon.parkingpass.parking.model

import java.time.Instant

/**
 * 차량 1회의 입차~출차를 표현하는 세션.
 *
 * ENTER 시 한 줄 생성되고, EXIT 시 같은 줄이 업데이트된다.
 *
 * @property parkingLotId 어떤 주차장에서 발생한 세션인지 (parking_lot.id)
 * @property plateNumber 정규화된 번호판
 * @property vehicleId 등록 차량이면 vehicle.id, 미등록이면 null
 * @property enterGateId 입차 게이트 (gate_device.id)
 * @property exitGateId 출차 게이트 (gate_device.id), 아직 출차 전이면 null
 * @property enteredAt 입차 시각
 * @property exitedAt 출차 시각, 아직 출차 전이면 null
 * @property status OPEN(주차 중), CLOSED(출차 완료)
 */
data class ParkingSession(
    val id: Long,
    val parkingLotId: Long,
    val plateNumber: String,
    val vehicleId: Long?,
    val enterGateId: Long,
    val exitGateId: Long?,
    val enteredAt: Instant,
    val exitedAt: Instant?,
    val status: SessionStatus
)
