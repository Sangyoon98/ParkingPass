package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingSession
import java.time.LocalDate

interface ParkingSessionRepository {
    suspend fun findOpenSessionByParkingLotIdAndPlateNumber(
        parkingLotId: Long,
        plateNumber: String
    ): ParkingSession?

    suspend fun save(session: ParkingSession): ParkingSession

    suspend fun findAllOpenSessions(parkingLotId: Long): List<ParkingSession>

    suspend fun findAllByParkingLotIdAndDate(
        parkingLotId: Long,
        date: LocalDate
    ): List<ParkingSession>
}