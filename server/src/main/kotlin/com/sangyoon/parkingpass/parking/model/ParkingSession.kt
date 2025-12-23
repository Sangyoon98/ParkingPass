package com.sangyoon.parkingpass.parking.model

import com.sangyoon.parkingpass.common.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
@Serializable
data class ParkingSession(
    val id: Long = 0,
    @SerialName("parking_lot_id")
    val parkingLotId: Long,
    @SerialName("plate_number")
    val plateNumber: String,
    @SerialName("vehicle_id")
    val vehicleId: Long? = null,
    @SerialName("enter_gate_id")
    val enterGateId: Long,
    @SerialName("exit_gate_id")
    val exitGateId: Long? = null,
    @SerialName("entered_at")
    @Serializable(with = InstantSerializer::class)
    val enteredAt: Instant,
    @SerialName("exited_at")
    @Serializable(with = InstantSerializer::class)
    val exitedAt: Instant? = null,
    val status: SessionStatus
)
