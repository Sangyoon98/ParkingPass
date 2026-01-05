package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingLot

interface ParkingLotRepository {
    suspend fun save(lot: ParkingLot): ParkingLot
    suspend fun findById(id: Long): ParkingLot?
    suspend fun findAll(): List<ParkingLot>
    suspend fun findByIds(ids: Collection<Long>): List<ParkingLot>
    suspend fun searchPublicLots(query: String, limit: Int = 20): List<ParkingLot>
    suspend fun deleteById(id: Long)
}
