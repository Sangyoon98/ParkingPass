package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingSession
import java.time.LocalDate

interface ParkingSessionRepository {
    fun findOpenSessionByParkingLotIdAndPlateNumber(
        parkingLotId: Long,
        plateNumber: String
    ): ParkingSession?

    fun save(session: ParkingSession): ParkingSession

    fun findAllOpenSessions(parkingLotId: Long): List<ParkingSession>

    fun findAllByParkingLotIdAndDate(
        parkingLotId: Long,
        date: LocalDate
    ): List<ParkingSession>
}