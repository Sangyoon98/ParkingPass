package com.sangyoon.parkingpass.session.service

import com.sangyoon.parkingpass.common.KOREA_ZONE_ID
import com.sangyoon.parkingpass.parking.model.ParkingSession
import com.sangyoon.parkingpass.parking.repository.ParkingSessionRepository
import com.sangyoon.parkingpass.parking.repository.VehicleRepository
import com.sangyoon.parkingpass.session.dto.SessionResponse
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SessionService(
    private val sessionRepository: ParkingSessionRepository,
    private val vehicleRepository: VehicleRepository
) {
    suspend fun getOpenSessions(parkingLotId: Long): List<SessionResponse> {
        val sessions = sessionRepository.findAllOpenSessions(parkingLotId)
        return sessions.map { toResponse(it) }
    }

    suspend fun getSessionHistory(parkingLotId: Long, date: String): List<SessionResponse> {
        // 날짜 파싱 (예: "2025-12-03")
        val targetDate = try {
            LocalDate.parse(date)
        } catch (e: Exception) {
            throw IllegalArgumentException("잘못된 날짜 형식입니다. YYYY-MM-DD 형식을 사용하세요: $date")
        }

        val sessions = sessionRepository.findAllByParkingLotIdAndDate(
            parkingLotId = parkingLotId,
            date = targetDate
        )

        return sessions.map { toResponse(it) }
    }

    suspend fun getCurrentSessionByPlateNumber(parkingLotId: Long, plateNumber: String): SessionResponse? {
        // 번호판 정규화 (공백 제거)
        val normalizedPlate = plateNumber.replace(" ", "")
        println("🔍 [SessionService] 세션 조회 - parkingLotId: $parkingLotId, 원본: '$plateNumber', 정규화: '$normalizedPlate'")
        
        val session = sessionRepository.findOpenSessionByParkingLotIdAndPlateNumber(
            parkingLotId = parkingLotId,
            plateNumber = normalizedPlate
        )
        
        if (session != null) {
            println("✅ [SessionService] 세션 찾음: id=${session.id}, plateNumber=${session.plateNumber}")
        } else {
            println("❌ [SessionService] 세션을 찾을 수 없음")
        }
        
        return session?.let { toResponse(it) }
    }

    private suspend fun toResponse(session: ParkingSession): SessionResponse {
        val vehicle = session.vehicleId?.let {
            vehicleRepository.findByParkingLotIdAndPlateNumber(
                parkingLotId = session.parkingLotId,
                plateNumber = session.plateNumber
            )
        }

        return SessionResponse(
            id = session.id,
            parkingLotId = session.parkingLotId,
            plateNumber = session.plateNumber,
            vehicleId = session.vehicleId,
            vehicleLabel = vehicle?.label,
            vehicleCategory = vehicle?.category,
            enterGateId = session.enterGateId,
            exitGateId = session.exitGateId,
            enteredAt = formatInstant(session.enteredAt),
            exitedAt = session.exitedAt?.let { formatInstant(it) },
            status = session.status.name
        )
    }

    private fun formatInstant(instant: Instant): String {
        // 한국 시간대로 변환 후 ISO8601 형식으로 포맷팅
        val koreaTime = instant.atZone(KOREA_ZONE_ID)
        return koreaTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}