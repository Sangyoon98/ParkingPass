package com.sangyoon.parkingpass.parkinglot.service

import com.sangyoon.parkingpass.parking.model.ParkingLot
import com.sangyoon.parkingpass.parking.repository.ParkingLotRepository
import com.sangyoon.parkingpass.parkinglot.dto.CreateParkingLotRequest
import com.sangyoon.parkingpass.parkinglot.dto.ParkingLotResponse

class ParkingLotService(
    private val parkingLotRepository: ParkingLotRepository
) {
    fun createParkingLot(request: CreateParkingLotRequest): ParkingLotResponse {
        val lot = ParkingLot(
            id = 0L,
            name = request.name,
            location = request.location
        )

        val saved = parkingLotRepository.save(lot)
        return toResponse(saved)
    }

    fun getAllParkingLots(): List<ParkingLotResponse> {
        return parkingLotRepository.findAll().map { toResponse(it) }
    }

    fun getParkingLot(id: Long): ParkingLotResponse {
        val lot = parkingLotRepository.findById(id)
            ?: throw IllegalArgumentException("주차장을 찾을 수 없습니다: $id")
        return toResponse(lot)
    }

    private fun toResponse(lot: ParkingLot): ParkingLotResponse =
        ParkingLotResponse(
            id = lot.id,
            name = lot.name,
            location = lot.location
        )
}