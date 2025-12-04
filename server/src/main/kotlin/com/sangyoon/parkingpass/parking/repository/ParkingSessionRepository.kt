package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingSession

interface ParkingSessionRepository {
    fun findOpenSessionByParkingLotIdAndPlateNumber(parkingLotId: Long, plateNumber: String): ParkingSession?
    fun save(session: ParkingSession): ParkingSession
    fun findAllOpenSessions(parkingLotId: Long): List<ParkingSession>
}