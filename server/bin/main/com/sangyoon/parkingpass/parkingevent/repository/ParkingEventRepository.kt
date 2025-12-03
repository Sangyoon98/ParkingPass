package com.sangyoon.parkingpass.parkingevent.repository

import com.sangyoon.parkingpass.parkingevent.model.ParkingEvent

interface ParkingEventRepository {
    fun save(event: ParkingEvent): ParkingEvent
    fun findAll(): List<ParkingEvent>
}