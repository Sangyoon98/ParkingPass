package com.sangyoon.parkingpass.parkingevent.sevice

import com.sangyoon.parkingpass.common.GateNotFoundException
import com.sangyoon.parkingpass.parking.model.GateDirection
import com.sangyoon.parkingpass.parking.model.ParkingSession
import com.sangyoon.parkingpass.parking.model.SessionStatus
import com.sangyoon.parkingpass.parking.repository.GateDeviceRepository
import com.sangyoon.parkingpass.parking.repository.ParkingSessionRepository
import com.sangyoon.parkingpass.parking.repository.VehicleRepository
import com.sangyoon.parkingpass.parkingevent.dto.PlateDetectedResponse
import com.sangyoon.parkingpass.parkingevent.model.ParkingAction
import com.sangyoon.parkingpass.parkingevent.model.ParkingEvent
import com.sangyoon.parkingpass.parkingevent.repository.ParkingEventRepository
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class ParkingEventService(
    private val eventRepository: ParkingEventRepository,
    private val gateDeviceRepository: GateDeviceRepository,
    private val vehicleRepository: VehicleRepository,
    private val sessionRepository: ParkingSessionRepository
) {
    private val eventIdGenerator = AtomicLong(1L)

    suspend fun handlePlateDetected(
        deviceKey: String,
        plateNumber: String,
        capturedAt: Instant
    ): PlateDetectedResponse {
        // 1. 번호판 정규화
        val normalizedPlate = normalizePlateNumber(plateNumber)

        // 2. deviceKey -> 게이트 조회
        val gate = gateDeviceRepository.findByDeviceKey(deviceKey)
            ?: throw GateNotFoundException(deviceKey)

        val parkingLotId = gate.parkingLotId

        // 3. OPEN 세션 존재 여부 확인
        val openSession = sessionRepository.findOpenSessionByParkingLotIdAndPlateNumber(
            parkingLotId = parkingLotId,
            plateNumber = normalizedPlate
        )

        // 4. 등록 차령 여부 확인
        val vehicle = vehicleRepository.findByParkingLotIdAndPlateNumber(
            parkingLotId = parkingLotId,
            plateNumber = normalizedPlate
        )

        // 5. 게이트 방향에 따른 ENTER/EXIT 판정
        val (session, action) = when {
            // ENTER 가능: 게이트가 ENTER 또는 BOTH이고, OPEN 세션이 없을 때
            (gate.direction == GateDirection.ENTER || gate.direction == GateDirection.BOTH) && openSession == null -> {
                val newSession = ParkingSession(
                    id = 0L,
                    parkingLotId = parkingLotId,
                    plateNumber = normalizedPlate,
                    vehicleId = vehicle?.id,
                    enterGateId = gate.id,
                    exitGateId = null,
                    enteredAt = capturedAt,
                    exitedAt = null,
                    status = SessionStatus.OPEN
                )
                val saved = sessionRepository.save(newSession)
                saved to ParkingAction.ENTER
            }

            // EXIT 가능: 게이트가 EXIT 또는 BOTH이고, OPEN 세션이 있을 때
            (gate.direction == GateDirection.EXIT || gate.direction == GateDirection.BOTH) && openSession != null -> {
                val closed = openSession.copy(
                    exitGateId = gate.id,
                    exitedAt = capturedAt,
                    status = SessionStatus.CLOSED
                )
                val saved = sessionRepository.save(closed)
                saved to ParkingAction.EXIT
            }

            // 잘못된 상황
            openSession == null && (gate.direction == GateDirection.EXIT) -> {
                throw IllegalArgumentException("출구 게이트에서 입차할 수 없습니다. 입구 게이트를 사용하세요.")
            }

            openSession != null && (gate.direction == GateDirection.ENTER) -> {
                throw IllegalArgumentException("입구 게이트에서 출차할 수 없습니다. 출구 게이트를 사용하세요.")
            }

            else -> {
                throw IllegalStateException("예상치 못한 상황입니다.")
            }
        }

        // 6. ParkingEvent 로그 저장
        val event = ParkingEvent(
            id = eventIdGenerator.getAndIncrement(),
            deviceKey = deviceKey,
            plateNumber = normalizedPlate,
            action = action
        )
        eventRepository.save(event)

        // 7. 응답 생성
        return PlateDetectedResponse(
            action = action,
            sessionId = session.id,
            plateNumber = normalizedPlate,
            isRegistered = (vehicle != null),
            vehicleLabel = vehicle?.label,
            vehicleCategory = vehicle?.category
        )
    }

    private fun normalizePlateNumber(plate: String): String {
        return plate.replace(" ", "")
    }
}