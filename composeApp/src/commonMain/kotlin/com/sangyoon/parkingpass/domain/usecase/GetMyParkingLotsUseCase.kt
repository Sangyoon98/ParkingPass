package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.ParkingLot
import com.sangyoon.parkingpass.domain.repository.ParkingLotRepository

class GetMyParkingLotsUseCase(
    private val repository: ParkingLotRepository
) {
    suspend operator fun invoke(): Result<List<ParkingLot>> = repository.getMyParkingLots()
}
