package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingLot

interface ParkingLotRepository {
    suspend fun save(lot: ParkingLot): ParkingLot
    suspend fun findById(id: Long): ParkingLot?
    suspend fun findAll(): List<ParkingLot>
}