package com.sangyoon.parkingpass.parkingevent.repository

import com.sangyoon.parkingpass.parkingevent.model.ParkingEvent
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryParkingEventRepository : ParkingEventRepository {
    private val events = CopyOnWriteArrayList<ParkingEvent>()

    override suspend fun save(event: ParkingEvent): ParkingEvent {
        events.add(event)
        return event
    }

    override suspend fun findAll(): List<ParkingEvent> = events.toList()
}