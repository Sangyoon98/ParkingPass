package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.common.KOREA_ZONE_ID
import com.sangyoon.parkingpass.parking.model.ParkingSession
import com.sangyoon.parkingpass.parking.model.SessionStatus
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryParkingSessionRepository : ParkingSessionRepository {
    private val idGenerator = AtomicLong(1L)
    private val sessions = ConcurrentHashMap<Long, ParkingSession>()

    override suspend fun findOpenSessionByParkingLotIdAndPlateNumber(
        parkingLotId: Long,
        plateNumber: String
    ): ParkingSession? = sessions.values.firstOrNull {
        it.parkingLotId == parkingLotId && it.plateNumber == plateNumber && it.status == SessionStatus.OPEN
    }

    override suspend fun save(session: ParkingSession): ParkingSession {
        val id = if (session.id != 0L) session.id else idGenerator.getAndIncrement()
        val saved = session.copy(id = id)
        sessions[id] = saved
        return saved
    }

    override suspend fun findAllOpenSessions(parkingLotId: Long): List<ParkingSession> =
        sessions.values.filter { it.parkingLotId == parkingLotId && it.status == SessionStatus.OPEN }

    override suspend fun findAllByParkingLotIdAndDate(
        parkingLotId: Long,
        date: LocalDate
    ): List<ParkingSession> {
        val startOfDay = date.atStartOfDay(KOREA_ZONE_ID).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(KOREA_ZONE_ID).toInstant()

        return sessions.values.filter { session ->
            session.parkingLotId == parkingLotId && session.enteredAt.isAfter(startOfDay) && session.enteredAt.isBefore(endOfDay)
        }
    }
}