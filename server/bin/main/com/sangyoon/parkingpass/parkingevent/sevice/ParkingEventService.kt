package com.sangyoon.parkingpass.parkingevent.sevice

import com.sangyoon.parkingpass.parkingevent.model.ParkingAction
import com.sangyoon.parkingpass.parkingevent.model.ParkingEvent
import com.sangyoon.parkingpass.parkingevent.repository.ParkingEventRepository
import java.util.concurrent.atomic.AtomicLong

class ParkingEventService(
    private val repository: ParkingEventRepository
) {
    private val idGenerator = AtomicLong(1L)

    fun handlePlateDetected(
        deviceKey: String,
        plateNumber: String
    ): ParkingEvent {
        // 학습용: 단순 로직 - 짝수 길이면 ENTER, 홀수 길이면 EXIT
        val normalized = normalizePlateNumber(plateNumber)
        val action = if (normalized.length % 2 == 0) ParkingAction.ENTER else ParkingAction.EXIT
        val event = ParkingEvent(
            id = idGenerator.getAndIncrement(),
            deviceKey = deviceKey,
            plateNumber = normalized,
            action = action
        )
        return repository.save(event)
    }

    private fun normalizePlateNumber(plate: String): String {
        return plate.replace(" ", "")
    }
}