package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingSession
import com.sangyoon.parkingpass.parking.model.SessionStatus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryParkingSessionRepository : ParkingSessionRepository {
    private val idGenerator = AtomicLong(1L)
    private val sessions = ConcurrentHashMap<Long, ParkingSession>()

    override fun findOpenSessionByParkingLotIdAndPlateNumber(
        parkingLotId: Long,
        plateNumber: String
    ): ParkingSession? = sessions.values.firstOrNull {
        it.parkingLotId == parkingLotId && it.plateNumber == plateNumber && it.status == SessionStatus.OPEN
    }

    override fun save(session: ParkingSession): ParkingSession {
        val id = if (session.id != 0L) session.id else idGenerator.getAndIncrement()
        val saved = session.copy(id = id)
        sessions[id] = saved
        return saved
    }

    override fun findAllOpenSessions(parkingLotId: Long): List<ParkingSession> =
        sessions.values.filter { it.parkingLotId == parkingLotId && it.status == SessionStatus.OPEN }
}