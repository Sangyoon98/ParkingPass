package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingLot

interface ParkingLotRepository {
    fun save(lot: ParkingLot): ParkingLot
    fun findById(id: Long): ParkingLot?
    fun findAll(): List<ParkingLot>
}