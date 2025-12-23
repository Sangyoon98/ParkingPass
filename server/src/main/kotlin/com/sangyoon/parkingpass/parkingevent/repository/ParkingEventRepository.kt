package com.sangyoon.parkingpass.parkingevent.repository

import com.sangyoon.parkingpass.parkingevent.model.ParkingEvent

interface ParkingEventRepository {
    suspend fun save(event: ParkingEvent): ParkingEvent
    suspend fun findAll(): List<ParkingEvent>
}